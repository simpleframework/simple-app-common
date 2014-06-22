package net.simpleframework.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.simpleframework.ado.IADOManagerFactory;
import net.simpleframework.ado.db.DbManagerFactory;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.ado.db.cache.MapDbEntityManager;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.ctx.ModuleContextFactory;
import net.simpleframework.ctx.permission.IPermissionHandler;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.mvc.MVCContext;

import org.hsqldb.Server;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractApplicationContext extends MVCContext implements IApplicationContext {

	@Override
	public ApplicationSettings getContextSettings() {
		/**
		 * 定义配置
		 */
		return singleton(ApplicationSettings.class);
	}

	@Override
	public Collection<IModuleContext> allModules() {
		return ModuleContextFactory.allModules();
	}

	@Override
	public <T extends IModuleContext> T getModuleContext(final Class<T> mClass) {
		return ModuleContextFactory.get(mClass);
	}

	@Override
	public IModuleContext getModuleContext(final String module) {
		return ModuleContextFactory.get(module);
	}

	@Override
	public DataSource getDataSource() {
		/**
		 * 定义数据源,每个ModuleContext可以设置自己的数据源
		 */
		return getContextSettings().getDataSource();
	}

	@Override
	public ITaskExecutor getTaskExecutor() {
		/**
		 * 获取任务接口
		 */
		return getContextSettings().getTaskExecutor();
	}

	@Override
	public IADOManagerFactory getADOManagerFactory() {
		return getADOManagerFactory(getDataSource());
	}

	private static Map<DataSource, DbManagerFactory> mFactoryCache = new HashMap<DataSource, DbManagerFactory>();

	@Override
	public IADOManagerFactory getADOManagerFactory(final DataSource dataSource) {
		/**
		 * 这里提供一个全局的IADOManagerFactory实现,各context可有自己的
		 */
		DbManagerFactory factory = mFactoryCache.get(dataSource);
		if (factory == null) {
			mFactoryCache.put(dataSource, factory = new DbManagerFactory(dataSource) {
				@Override
				public IDbEntityManager<?> createEntityManager(final Class<?> beanClass) {
					IDbEntityManager<?> eManager = null;
					final String db = getContextSettings().getProperty(
							ApplicationSettings.DBENTITYMANAGER_HANDLER);
					if (StringUtils.hasText(db)) {
						try {
							eManager = (IDbEntityManager<?>) ObjectFactory.create(ClassUtils.forName(db));
						} catch (final ClassNotFoundException e) {
						}
					}
					if (eManager == null) {
						eManager = ObjectFactory.create(MapDbEntityManager.class);
					}
					onCreateEntityManager(eManager);
					return eManager;
				}
			});
		}
		return factory;
	}

	protected void onCreateEntityManager(final IDbEntityManager<?> eManager) {
		System.out.println("-----" + eManager.getClass().getName() + "-----");
	}

	@Override
	public int getDomain() {
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends IPermissionHandler> getPagePermissionHandler() {
		/**
		 * 定义权限的实现类
		 */
		try {
			return (Class<? extends IPermissionHandler>) ClassUtils.forName(getContextSettings()
					.getPermissionHandler());
		} catch (final ClassNotFoundException e) {
			return super.getPagePermissionHandler();
		}
	}

	@Override
	protected void onApplicationInit() throws Exception {
		// 启动测试用的hsql数据库,生产环境不需要
		doHsql();
		super.onApplicationInit();
	}

	protected void doHsql() {
		final ApplicationSettings settings = getContextSettings();
		if (!settings.getBoolProperty("hsql.start")) {
			return;
		}
		try {
			System.setProperty("hsqldb.reconfig_logging", "false");
			final Server svr = new Server();
			svr.setAddress(settings.getProperty("hsql.address"));
			svr.setPort(settings.getIntProperty("hsql.port"));
			svr.setDatabaseName(0, settings.getProperty("hsql.dbname"));
			svr.setDatabasePath(0, settings.getProperty("hsql.dbpath"));
			svr.setSilent(true);
			svr.start();
		} catch (final Exception e1) {
			log.warn(e1);
		}
	}
}
