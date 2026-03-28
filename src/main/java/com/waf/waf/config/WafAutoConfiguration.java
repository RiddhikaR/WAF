package com.waf.waf.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import jakarta.servlet.http.HttpServletRequest;

@AutoConfiguration
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(WafProperties.class)
@ComponentScan(basePackages = "com.waf.waf")
public class WafAutoConfiguration {
}