package com.alisiikh.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author lial
 */
public final class UrlUtils {

	public static String encode(String param) {
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return param;
		}
	}
}
