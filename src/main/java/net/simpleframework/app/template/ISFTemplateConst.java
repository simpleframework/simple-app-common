package net.simpleframework.app.template;

import net.simpleframework.mvc.common.element.LinkElement;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface ISFTemplateConst {

	static LinkElement SF_LINK = LinkElement.BLANK("http://simpleframework.net").setHref(
			"http://simpleframework.net");

	static LinkElement CHROME_LINK = LinkElement.BLANK("Chrome").setHref(
			"http://www.google.com/chrome/");

	static LinkElement FIREFOX_LINK = LinkElement.BLANK("Firefox").setHref("http://firefox.com.cn/");
}
