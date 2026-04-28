package com.guavasoft.springbatch.dashboard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DataSourceContextFilter extends OncePerRequestFilter {

    public static final String ENVIRONMENT_HEADER = "X-Environment";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String environment = request.getHeader(ENVIRONMENT_HEADER);
        if (StringUtils.isNotBlank(environment)) {
            DataSourceContext.set(environment);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            DataSourceContext.clear();
        }
    }
}
