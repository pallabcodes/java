package com.netflix.systemdesign.logging;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Servlet filter to ensure correlation id is present and propagated.
 */
public class CorrelationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String incoming = null;
            if (request instanceof HttpServletRequest) {
                incoming = ((HttpServletRequest) request).getHeader("X-Correlation-Id");
            }
            CorrelationContext.startOrResume(incoming);
            chain.doFilter(request, response);
        } finally {
            CorrelationContext.clear();
        }
    }
}


