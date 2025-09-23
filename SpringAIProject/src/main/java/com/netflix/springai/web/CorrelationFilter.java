package com.netflix.springai.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter implements Filter {
	public static final String CORRELATION_ID = "X-Correlation-Id";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest http = (HttpServletRequest) request;
		String correlationId = http.getHeader(CORRELATION_ID);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}
		MDC.put("correlationId", correlationId);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove("correlationId");
		}
	}
}
