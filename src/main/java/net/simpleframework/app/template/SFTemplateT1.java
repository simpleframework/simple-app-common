package net.simpleframework.app.template;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IMVCConst;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.t1.AbstractTemplateHandlerT1;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class SFTemplateT1 extends AbstractTemplateHandlerT1 implements ISFTemplateConst {
	private static SFTemplateT1 t1;

	public SFTemplateT1() {
		t1 = this;
	}

	@Override
	public Class<? extends AbstractMVCPage> getHeaderPage() {
		return HeaderPageT1.class;
	}

	@Override
	public Class<? extends AbstractMVCPage> getFooterPage() {
		return FooterPageT1.class;
	}

	@Override
	public MenuItems getMainMenuItems(final ComponentParameter cp, final MenuItem menuItem) {
		// 覆盖此函数更改T1模板的菜单数据
		// 父类根据装载模块自动组装菜单
		return super.getMainMenuItems(cp, menuItem);
	}

	protected String toHeaderHtml(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append(" <div class='logo'></div>");
		sb.append(" <div class='re'>Menu</div>");
		sb.append(BlockElement.CLEAR);
		return sb.toString();
	}

	protected String toFooterHTML(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append(" <div class='ll'>");
		sb.append(SF_LINK).append("</div>");
		sb.append("	<div class='lr'>");
		sb.append(CHROME_LINK).append(", ").append(FIREFOX_LINK);
		sb.append("#(FooterPage.0) [ ").append(
				new SpanElement().setId("idSFTemplateT1_loadTime").addStyle("color: #900;"));
		sb.append("&nbsp;s ]");
		sb.append(" </div>");
		sb.append(BlockElement.CLEAR);
		sb.append(HtmlConst.TAG_SCRIPT_START);
		sb.append("$ready(function() { $('idSFTemplateT1_loadTime').innerHTML = document.getCookie('")
				.append(IMVCConst.PAGELOAD_TIME).append("'); });");
		sb.append(HtmlConst.TAG_SCRIPT_END);
		return sb.toString();
	}

	public static class HeaderPageT1 extends AbstractHeaderPage {

		@Override
		protected void onForward(final PageParameter pp) {
			super.onForward(pp);

			pp.addImportCSS(SFTemplateT1.class, "/t1.css");
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			return t1.toHeaderHtml(pp);
		}
	}

	public static class FooterPageT1 extends AbstractTemplatePage {

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			return t1.toFooterHTML(pp);
		}
	}
}
