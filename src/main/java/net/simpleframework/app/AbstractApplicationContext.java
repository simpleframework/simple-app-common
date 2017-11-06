package net.simpleframework.app;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.sql.DataSource;

import org.hsqldb.Server;

import net.simpleframework.ado.IADOManagerFactory;
import net.simpleframework.ado.db.DbManagerFactory;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.ado.db.cache.IDbEntityCache;
import net.simpleframework.ado.db.cache.MapDbEntityManager;
import net.simpleframework.ado.db.jdbc.DefaultJdbcProvider;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.Version;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.ctx.ContextUtils;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.ModuleContextFactory;
import net.simpleframework.ctx.ModuleRefUtils;
import net.simpleframework.ctx.permission.IPermissionHandler;
import net.simpleframework.ctx.settings.IContextSettingsConst;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.mvc.IFilterListener;
import net.simpleframework.mvc.MVCContext;
import net.simpleframework.mvc.MVCUtils;
import net.simpleframework.mvc.PageRequestResponse;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractApplicationContext extends MVCContext implements IApplicationContext {

	@Override
	protected void onBeforeInit() throws Exception {
		super.onBeforeInit();

		// 初始化IModuleRef
		ModuleRefUtils.doRefInit(this);

		getContextSettings().onInit(this);
	}

	@Override
	protected void onAfterInit() throws Exception {
		super.onAfterInit();

		// 启动测试用的hsql数据库,生产环境不需要
		doHsql();
		ContextUtils.doInit(this);
	}

	@Override
	public ApplicationSettings getContextSettings() {
		/**
		 * 定义配置
		 */
		return singleton(ApplicationSettings.class);
	}

	@Override
	public DataSource getDataSource(final String key) {
		/**
		 * 定义数据源,每个ModuleContext可以设置自己的数据源
		 */
		return getContextSettings().getDataSource(key);
	}

	@Override
	public DataSource getDataSource() {
		return getDataSource(IContextSettingsConst.DBPOOL);
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

	private static Map<DataSource, DbManagerFactory> mFactoryCache = new HashMap<>();

	@Override
	public IADOManagerFactory getADOManagerFactory(final DataSource dataSource) {
		// 这里提供一个全局的IADOManagerFactory实现,各context可有自己的
		DbManagerFactory factory = mFactoryCache.get(dataSource);
		if (factory == null) {
			final ApplicationSettings settings = getContextSettings();
			mFactoryCache.put(dataSource, factory = new DbManagerFactory(dataSource) {

				@Override
				protected DefaultJdbcProvider createJdbcProvider(final DataSource dataSource) {
					return new DefaultJdbcProvider(dataSource) {
						@Override
						protected long getSlowTimeMillis() {
							return Convert.toLong(
									settings.getProperty(IContextSettingsConst.PRINT_SQL_TIMEMILLIS),
									super.getSlowTimeMillis());
						}
					};
				}

				@Override
				public IDbEntityManager<?> createEntityManager(final Class<?> beanClass) {
					IDbEntityManager<?> eManager = null;
					final String db = settings.getProperty(settings.getDsKey(dataSource) + "."
							+ IContextSettingsConst.DBPOOL_ENTITYMANAGER);
					if (StringUtils.hasText(db)) {
						eManager = (IDbEntityManager<?>) ObjectFactory.create(db);
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

	@Override
	public IADOManagerFactory getADOManagerFactory(final String key) {
		return getADOManagerFactory(getDataSource(key));
	}

	protected void onCreateEntityManager(final IDbEntityManager<?> eManager) {
	}

	@Override
	public String getDomain() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends IPermissionHandler> getPagePermissionHandler() {
		/**
		 * 定义权限的实现类
		 */
		try {
			return (Class<? extends IPermissionHandler>) ClassUtils
					.forName(getContextSettings().getPermissionHandler());
		} catch (final ClassNotFoundException e) {
			return super.getPagePermissionHandler();
		}
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
			getLog().warn(e1);
		}
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
	public Version getVersion() {
		final String ver = getContextSettings().getProperty(IContextSettingsConst.CTX_VERSION);
		if (StringUtils.hasText(ver)) {
			return Version.getVersion(ver);
		}
		return super.getVersion();
	}

	@Override
	public IModuleRef getPDFRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.pdf.web.PDFWebRef");
	}

	private File rootDir;

	@Override
	public File getRootDir() {
		if (rootDir == null) {
			rootDir = new File(MVCUtils.getRealPath("/"));
		}
		return rootDir;
	}

	public class ReqCacheFilterListener implements IFilterListener {

		@Override
		public EFilterResult doFilter(final PageRequestResponse rRequest,
				final FilterChain filterChain) throws IOException {
			// redis 缓存
			KVMap kv = (KVMap) rRequest.getSessionAttr("REQUEST_THREAD_CACHE");
			if (kv == null || rRequest.isHttpRequest() || rRequest.isAjaxRequest()) {
				rRequest.setSessionAttr("REQUEST_THREAD_CACHE", kv = new KVMap());
			}
			IDbEntityCache.REQUEST_THREAD_CACHE.set(kv);
			return EFilterResult.SUCCESS;
		}
	}
}
