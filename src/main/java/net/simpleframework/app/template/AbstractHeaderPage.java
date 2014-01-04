package net.simpleframework.app.template;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.LinkElementEx;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.organization.web.page.attri.AbstractEditAwarePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractHeaderPage extends AbstractEditAwarePage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		addComponent_logout(pp);
	}

	public AbstractElement<?> str_User(final PageParameter pp) {
		return new SpanElement().addElements(new SpanElement($m("AbstractHeaderPage.0")),
				new LinkElementEx(pp.getLogin()).setMenuIcon(true));
	}

	public static String js_shake(final String selector) {
		final StringBuilder sb = new StringBuilder();
		sb.append("$ready(function() {");
		sb.append(" var sup = $('").append(selector).append("'); if (!sup) return;");
		sb.append(" var i = 0;");
		sb.append(" var sh = setInterval(function() {");
		sb.append("  sup.$shake({ distance: 4 });");
		sb.append("  if (i++ > 10) clearInterval(sh);");
		sb.append(" }, 2000);");
		sb.append("});");
		return sb.toString();
	}
}