package com.netflix.backend.auth.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

	public static String getClientIp(HttpServletRequest request) {

		String xfHeader = request.getHeader("X-Forwarded-For");

		if (xfHeader == null) {
			return request.getRemoteAddr();
		}

		return xfHeader.split(",")[0];
	}
	
	public static String getDevice(HttpServletRequest request) {
	    return request.getHeader("User-Agent");
	}
}