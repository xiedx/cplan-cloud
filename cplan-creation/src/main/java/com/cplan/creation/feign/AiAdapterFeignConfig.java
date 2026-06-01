package com.cplan.creation.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Feign request interceptor that propagates X-User-Id and other headers
 * from the current HTTP request to downstream Feign calls.
 */
@Component
public class AiAdapterFeignConfig implements RequestInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            propagateHeader(request, template, HEADER_USER_ID);
            propagateHeader(request, template, HEADER_USERNAME);
        }
    }

    private void propagateHeader(HttpServletRequest source, RequestTemplate target, String headerName) {
        String value = source.getHeader(headerName);
        if (value != null) {
            target.header(headerName, value);
        }
    }
}
