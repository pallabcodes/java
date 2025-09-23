package com.netflix.springai.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessLogFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		long start = System.currentTimeMillis();
		try {
			chain.doFilter(request, response);
		} finally {
			long took = System.currentTimeMillis() - start;
			String cid = MDC.get("correlationId");
			log.info("method={} path={} status={} tookMs={} cid={}", req.getMethod(), req.getRequestURI(), res.getStatus(), took, cid);
		}
	}
}
