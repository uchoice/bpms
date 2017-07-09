package net.uchoice.thymeleaf.expression;

import javax.servlet.http.HttpServletRequest;

import net.uchoice.common.utils.CookieUtils;

import org.apache.commons.lang3.StringUtils;

public final class Cookies {

	/**
	 * 获得指定Cookie的值
	 * 
	 * @param name
	 *            名称
	 * @return 值
	 */
	public String get(HttpServletRequest request, String name) {
		return CookieUtils.getCookie(request, name);
	}

	/**
	 * 获得指定Cookie的值
	 * 
	 * @param name
	 *            名称
	 * @return 值
	 */
	public String get(HttpServletRequest request, String name,
			String defualtValue) {
		String s = CookieUtils.getCookie(request, name);
		if (StringUtils.isBlank(s)) {
			return defualtValue;
		}
		return s;
	}

}
