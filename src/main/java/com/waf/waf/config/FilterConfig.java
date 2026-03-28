package com.waf.waf.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.waf.waf.filter.WafFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<WafFilter> wafFilterRegistration(WafFilter wafFilter) {
        FilterRegistrationBean<WafFilter> registration = new FilterRegistrationBean<>(wafFilter);

        // Run before Spring Security, Actuator, and all other filters
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        // Apply to every request
        registration.addUrlPatterns("/*");
        
        registration.setName("WafFilter");

        return registration;
    }
}