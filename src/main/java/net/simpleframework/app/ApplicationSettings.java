package net.simpleframework.app;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.I18n;
import net.simpleframework.common.I18n.ILocaleHandler;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.SymmetricEncrypt;
import net.simpleframework.common.object.ObjectFactory;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.settings.ContextSettings;
import net.simpleframework.ctx.settings.IContextSettingsConst;
import net.simpleframework.ctx.settings.PropertiesContextSettings;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.ctx.task.TaskExecutor;
import net.simpleframework.mvc.IMVCContext;
import net.simpleframework.mvc.MVCSettings;
import net.simpleframework.mvc.MVCUtils;
import net.simpleframework.mvc.PageRequestResponse;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ApplicationSettings extends PropertiesContextSettings
		implements IContextSettingsConst {
	/* 数据源 */
	private final Map<String, DataSource> dsCache = new HashMap<String, DataSource>();
	/* 任务 */
	private ITaskExecutor taskExecutor;

	@Override
	public void onInit(final IApplicationContext context) throws Exception {
		super.onInit(context);

		// 创建mvc配置
		if (context instanceof AbstractApplicationContext) {
			createMVCSettings((AbstractApplicationContext) context);
		}

		// 初始化配置环境
		final File settingsFile = new File(MVCUtils.getRealPath("/WEB-INF/base.properties"));
		if (!settingsFile.exists()) {
			load(ClassUtils.getResourceAsStream("base.properties"));
		} else {
			load(new FileInputStream(settingsFile));
		}

		setHomeFileDir(new File(MVCUtils.getRealPath("/")));

		I18n.setLocaleHandler(new ILocaleHandler() {
			@Override
			public Locale getLocale() {
				return ApplicationSettings.this.getLocale();
			}
		});
	}

	protected MVCSettings createMVCSettings(final IMVCContext context) {
		return new _MVCSettings(context, this);
	}

	public static void main(final String[] args) {
		final SymmetricEncrypt des = new SymmetricEncrypt("simpleframework.net");
		// test...
		oprintln(des.encrypt("root"));
		oprintln(des.encrypt("root"));
	}

	private final SymmetricEncrypt des = createEncrypt();

	protected SymmetricEncrypt createEncrypt() {
		return new SymmetricEncrypt("simpleframework.net");
	}

	public String getDsKey(final DataSource dataSource) {
		for (final Map.Entry<String, DataSource> e : dsCache.entrySet()) {
			if (ObjectUtils.objectEquals(e.getValue(), dataSource)) {
				return e.getKey();
			}
		}
		return DBPOOL;
	}

	public DataSource getDataSource(final String key) {
		DataSource dataSource = dsCache.get(key);
		if (dataSource == null) {
			dsCache.put(key,
					dataSource = (DataSource) ObjectFactory.create(getProperty(DBPOOL_PROVIDER)));
			for (final String prop : StringUtils.split(getProperty(DBPOOL_PROPERTIES))) {
				final String val = getDsProperty(key, prop);
				if (val != null) {
					BeanUtils.setProperty(dataSource, prop, val);
				}
			}
		}
		return dataSource;
	}

	private String getDsProperty(final String key, final String prop) {
		String val = getProperty(key + "." + prop);
		if (val == null) {
			val = getProperty("~" + key + "." + prop);
			if (val != null) {
				val = des.decrypt(val);
			}
		}
		if (val == null && !DBPOOL.equals(key)) {
			val = getDsProperty(DBPOOL, prop);
		}
		return val;
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

	@Override
	public boolean isDebug() {
		return getBoolProperty(CTX_DEBUG, super.isDebug());
	}

	private Locale _locale;

	@Override
	public Locale getLocale() {
		if (_locale == null) {
			final String[] arr = StringUtils.split(getProperty(CTX_LOCALE), "_");
			if (arr.length > 1) {
				_locale = new Locale(arr[0], arr[1]);
			} else if (arr.length > 0) {
				_locale = new Locale(arr[0]);
			}
		}
		return _locale != null ? _locale : super.getLocale();
	}

	@Override
	public String getCharset() {
		return getProperty(CTX_CHARSET, super.getCharset());
	}

	private static String pid;
	static {
		final String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.substring(0, name.indexOf("@"));
	}

	@Override
	public String getContextNo() {
		// 获取服务编号
		return getProperty(CTX_NO, pid);
	}

	protected class _MVCSettings extends MVCSettings {
		private final ApplicationSettings _settings;

		public _MVCSettings(final IMVCContext context, final ContextSettings applicationSettings) {
			super(context, applicationSettings);
			_settings = (ApplicationSettings) applicationSettings;
		}

		@Override
		public boolean isResourceCompress() {
			return _settings.getBoolProperty(CTX_RESOURCECOMPRESS, super.isResourceCompress());
		}

		@Override
		public int getServerPort(final PageRequestResponse rRequest) {
			return _settings.getIntProperty(MVC_SERVERPORT, super.getServerPort(rRequest));
		}

		@Override
		public String getIEWarnPath(final PageRequestResponse rRequest) {
			return _settings.getProperty(MVC_IEWARNPATH, super.getIEWarnPath(rRequest));
		}

		@Override
		public String getLoginPath(final PageRequestResponse rRequest) {
			return _settings.getProperty(MVC_LOGINPATH, super.getLoginPath(rRequest));
		}

		@Override
		public String getHomePath(final PageRequestResponse rRequest) {
			return _settings.getProperty(MVC_HOMEPATH, super.getHomePath(rRequest));
		}

		@Override
		public String getFilterPath() {
			return _settings.getProperty(MVC_FILTERPATH, super.getFilterPath());
		}
	}
}
