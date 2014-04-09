package net.simpleframework.app.template;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.t2.AbstractTemplateHandlerT2;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class SFTemplateT2 extends AbstractTemplateHandlerT2 implements ISFTemplateConst {
	@Override
	public Class<? extends AbstractMVCPage> getHeaderPage() {
		return HeaderPageT2.class;
	}

	@Override
	public Class<? extends AbstractMVCPage> getFooterPage() {
		return FooterPageT2.class;
	}

	@Override
	public MenuItems getMainMenuItems(final ComponentParameter cp, final MenuItem menuItem) {
		// 覆盖此函数更改T2模板的菜单数据
		// 父类根据装载模块自动组装菜单
		return super.getMainMenuItems(cp, menuItem);
	}

	protected String toHeaderHtml(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='HeaderPageT2'>");
		sb.append(" <div class='logo'></div>");
		sb.append(" <div class='re'>Menu</div>");
		sb.append(BlockElement.CLEAR);
		sb.append("</div>");
		return sb.toString();
	}

	protected String toFooterHTML(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div>").append(SF_LINK).append("</div>");
		return sb.toString();
	}

	public static class HeaderPageT2 extends AbstractHeaderPage {

		@Override
		protected void onForward(final PageParameter pp) {
			super.onForward(pp);

			pp.addImportCSS(SFTemplateT2.class, "/t2.css");
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			return ((SFTemplateT2) AbstractTemplateHandlerT2.get()).toHeaderHtml(pp);
		}
	}

	public static class FooterPageT2 extends AbstractTemplatePage {

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			return ((SFTemplateT2) AbstractTemplateHandlerT2.get()).toFooterHTML(pp);
		}
	}
}
