package net.simpleframework.app;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import net.simpleframework.ado.IADOManagerFactory;
import net.simpleframework.ado.db.DbManagerFactory;
import net.simpleframework.app.template.SFTemplateT1;
import net.simpleframework.app.template.SFTemplateT2;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.ctx.ModuleContextFactory;
import net.simpleframework.ctx.permission.IPermissionHandler;
import net.simpleframework.ctx.script.IScriptEval;
import net.simpleframework.ctx.script.ScriptEvalFactory;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.ITemplateHandler;
import net.simpleframework.mvc.MVCContext;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.t2.T2TemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
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
	public ITemplateHandler getTemplate(final PageParameter pp) {
		/**
		 * 这个方法的作用是提供模板的Handler,前提是用simple缺省提供的2套模板,如果自己写模板,就没有意义了
		 */
		if (AbstractMVCPage.get(pp) instanceof T2TemplatePage) {
			return singleton(SFTemplateT2.class);
		}
		return singleton(SFTemplateT1.class);
	}

	@Override
	public Collection<IModuleContext> allModules() {
		return ModuleContextFactory.allModules();
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
	public IScriptEval createScriptEval(final Map<String, Object> variables) {
		return ScriptEvalFactory.createDefaultScriptEval(variables);
	}

	@Override
	public IADOManagerFactory getADOManagerFactory() {
		return getADOManagerFactory(getDataSource());
	}

	@Override
	public IADOManagerFactory getADOManagerFactory(final DataSource dataSource) {
		/**
		 * 这里提供一个全局的IADOManagerFactory实现,各context可有自己的
		 */
		return DbManagerFactory.get(dataSource);
	}

	@Override
	public String[] getScanPackageNames() {
		/**
		 * 这里覆盖你要扫描的资源包
		 */
		return new String[] { "net.simpleframework" };
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
}
