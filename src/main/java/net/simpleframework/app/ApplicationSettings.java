package net.simpleframework.app;

import java.io.File;
import java.io.FileInputStream;

import javax.sql.DataSource;

import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ClassUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.SymmetricEncrypt;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.settings.PropertiesContextSettings;
import net.simpleframework.ctx.task.ITaskExecutor;
import net.simpleframework.ctx.task.TaskExecutor;
import net.simpleframework.mvc.IMVCContext;
import net.simpleframework.mvc.IMVCContextVar;
import net.simpleframework.mvc.MVCSettings;
import net.simpleframework.mvc.MVCUtils;
import net.simpleframework.mvc.PageRequestResponse;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class ApplicationSettings extends PropertiesContextSettings implements IMVCContextVar {
	/* 数据源 */
	private DataSource dataSource;
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
	}

	protected MVCSettings createMVCSettings(final IMVCContext context) {
		final MVCSettings settings = new _MVCSettings(context);
		settings.setContextSettings(this);
		return settings;
	}

	public static void main(final String[] args) {
		final SymmetricEncrypt des = new SymmetricEncrypt("simpleframework.net");
		// test...
		System.out.println(des.encrypt("root"));
		System.out.println(des.encrypt("root"));
	}

	private final SymmetricEncrypt des = createEncrypt();

	protected SymmetricEncrypt createEncrypt() {
		return new SymmetricEncrypt("simpleframework.net");
	}

	public DataSource getDataSource() {
		if (dataSource == null) {
			try {
				dataSource = (DataSource) ClassUtils.forName(getProperty(DBPOOL_PROVIDER))
						.newInstance();
				for (final String prop : StringUtils.split(getProperty(DBPOOL_PROPERTIES))) {
					String val = getProperty(DBPOOL + "." + prop);
					if (val == null) {
						val = getProperty("~" + DBPOOL + "." + prop);
						if (val != null) {
							val = des.decrypt(val);
						}
					}
					if (val != null) {
						BeanUtils.setProperty(dataSource, prop, val);
					}
				}
			} catch (final Exception e) {
				getLog().error(e);
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

	@Override
	public boolean isDebug() {
		return getBoolProperty(CTX_DEBUG, super.isDebug());
	}

	@Override
	public String getCharset() {
		return getProperty(CTX_CHARSET, super.getCharset());
	}

	@Override
	public String getContextNo() {
		return getProperty(CTX_NO, super.getContextNo());
	}

	public static final String DBPOOL = "dbpool";
	public static final String DBPOOL_PROVIDER = "dbpool.provider";
	public static final String DBPOOL_PROPERTIES = "dbpool.properties";

	public static final String CTX_CHARSET = "ctx.charset";
	public static final String CTX_RESOURCECOMPRESS = "ctx.resourcecompress";
	public static final String CTX_PERMISSIONHANDLER = "ctx.permissionhandler";
	public static final String CTX_DEBUG = "ctx.debug";
	public static final String CTX_NO = "ctx.no";

	public static final String DBENTITYMANAGER_HANDLER = "db.entitymanager";

	public static final String MVC_SERVERPORT = "mvc.serverport";
	public static final String MVC_FILTERPATH = "mvc.filterpath";
	public static final String MVC_LOGINPATH = "mvc.loginpath";
	public static final String MVC_HOMEPATH = "mvc.homepath";
	public static final String MVC_IEWARNPATH = "mvc.iewarnpath";

	protected class _MVCSettings extends MVCSettings {
		public _MVCSettings(final IMVCContext context) {
			super(context);
		}

		@Override
		public boolean isResourceCompress() {
			return getBoolProperty(CTX_RESOURCECOMPRESS, super.isResourceCompress());
		}

		@Override
		public int getServerPort(final PageRequestResponse rRequest) {
			return getIntProperty(MVC_SERVERPORT, super.getServerPort(rRequest));
		}

		@Override
		public String getIEWarnPath(final PageRequestResponse rRequest) {
			return getProperty(MVC_IEWARNPATH, super.getIEWarnPath(rRequest));
		}

		@Override
		public String getLoginPath(final PageRequestResponse rRequest) {
			return getProperty(MVC_LOGINPATH, super.getLoginPath(rRequest));
		}

		@Override
		public String getHomePath(final PageRequestResponse rRequest) {
			return getProperty(MVC_HOMEPATH, super.getHomePath(rRequest));
		}

		@Override
		public String getFilterPath() {
			return getProperty(MVC_FILTERPATH, super.getFilterPath());
		}
	}
}
