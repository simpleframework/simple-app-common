package net.simpleframework.app;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IApplicationContextBase;
import net.simpleframework.ctx.settings.PropertiesContextSettings;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.ctx.task.TaskExecutor;
import net.simpleframework.mvc.IMVCContextVar;
import net.simpleframework.mvc.MVCSettings;
import net.simpleframework.mvc.PageRequestResponse;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ApplicationSettings extends PropertiesContextSettings implements IMVCContextVar {

	private ITaskExecutor taskExecutor;

	private DataSource dataSource;

	private MVCSettings settings;

	@Override
	public void onInit(final IApplicationContextBase context) throws Exception {
		super.onInit(context);

		settings = createMVCSettings();
		settings.onInit(context);

		// 初始化配置环境
		final File settingsFile = new File(settings.getRealPath("/WEB-INF/base.properties"));
		if (!settingsFile.exists()) {
			load(ClassUtils.getResourceAsStream("base.properties"));
		} else {
			load(new FileInputStream(settingsFile));
		}

		setHomeFileDir(new File(settings.getRealPath("/")));
	}

	protected MVCSettings createMVCSettings() {
		return new _MVCSettings(this);
	}

	public DataSource getDataSource() {
		if (dataSource == null) {
			try {
				dataSource = (DataSource) ClassUtils.forName(getProperty(DBPOOL_PROVIDER))
						.newInstance();
				for (final String prop : StringUtils.split(getProperty(DBPOOL_PROPERTIES))) {
					BeanUtils.setProperty(dataSource, prop, getProperty(DBPOOL + "." + prop));
				}
			} catch (final Exception e) {
				log.error(e);
			}
		}
		return dataSource;
	}

	public ITaskExecutor getTaskExecutor() {
		if (taskExecutor == null) {
			taskExecutor = new TaskExecutor();
		}
		return taskExecutor;
	}

	public String getPermissionHandler() {
		return getProperty(CTX_PERMISSIONHANDLER,
				"net.simpleframework.mvc.ctx.permission.DefaultPagePermissionHandler");
	}

	public static final String DBPOOL = "dbpool";
	public static final String DBPOOL_PROVIDER = "dbpool.provider";
	public static final String DBPOOL_PROPERTIES = "dbpool.properties";

	public static final String CTX_CHARSET = "ctx.charset";
	public static final String CTX_RESOURCECOMPRESS = "ctx.resourcecompress";
	public static final String CTX_PERMISSIONHANDLER = "ctx.permissionhandler";
	public static final String CTX_DEBUG = "ctx.debug";

	public static final String MVC_FILTERPATH = "mvc.filterpath";
	public static final String MVC_LOGINPATH = "mvc.loginpath";
	public static final String MVC_HOMEPATH = "mvc.homepath";
	public static final String MVC_IEWARNPATH = "mvc.iewarnpath";

	public static class _MVCSettings extends MVCSettings {
		private final ApplicationSettings settings;

		public _MVCSettings(final ApplicationSettings settings) {
			this.settings = settings;
		}

		@Override
		public File getHomeFileDir() {
			return settings.homeDir != null ? settings.homeDir : super.getHomeFileDir();
		}

		@Override
		public boolean isDebug() {
			return settings.getBoolProperty(CTX_DEBUG, super.isDebug());
		}

		@Override
		public boolean isResourceCompress() {
			return settings.getBoolProperty(CTX_RESOURCECOMPRESS, super.isResourceCompress());
		}

		@Override
		public String getCharset() {
			return settings.getProperty(CTX_CHARSET, super.getCharset());
		}

		@Override
		public String getLoginPath(final PageRequestResponse rRequest) {
			return settings.getProperty(MVC_LOGINPATH, super.getLoginPath(rRequest));
		}

		@Override
		public String getHomePath(final PageRequestResponse rRequest) {
			return settings.getProperty(MVC_HOMEPATH, super.getHomePath(rRequest));
		}

		@Override
		public String getFilterPath() {
			return settings.getProperty(MVC_FILTERPATH, super.getFilterPath());
		}

		@Override
		public String getIEWarnPath(final PageRequestResponse rRequest) {
			return settings.getProperty(MVC_IEWARNPATH, super.getIEWarnPath(rRequest));
		}

		@Override
		public Map<String, String> getFilterPackages() {
			return packages;
		}

		private final Map<String, String> packages = new LinkedHashMap<String, String>();
		{
			packages.put("/sf", "net.simpleframework");
		}
	}
}
