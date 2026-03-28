package com.waf.waf.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.NormalizeRequestDto;
import com.waf.waf.dto.WafResult;
import com.waf.waf.engine.WafEngine;
import com.waf.waf.logger.WafAuditLogger;
import com.waf.waf.service.BanService;
import com.waf.waf.service.ExtractService;
import com.waf.waf.service.NormalizeService;
import com.waf.waf.service.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WafFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final BanService banService;
    private final NormalizeService normalizeService;
    private final ExtractService extractService;
    private final WafEngine wafEngine;
    private final WafAuditLogger auditLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (httpServletRequest.getRequestURI().startsWith("/waf/")) {
        filterChain.doFilter(httpServletRequest, httpServletResponse);
        return;
    }
        String ip = httpServletRequest.getRemoteAddr();

        // Stage 1: Ban check
        if (banService.isBanned(ip)) {
            auditLogger.logEarlyReject(ip, 403, "IP is banned");
            httpServletResponse.sendError(403, "Forbidden");
            return;
        }

        // Stage 2: Rate limit check
        if (!rateLimitService.allowed(ip)) {
            banService.recordViolationVerdict(ip, 429);
            auditLogger.logEarlyReject(ip, 429, "Rate limit exceeded");
            applyRateLimitHeaders(httpServletResponse, ip);
            httpServletResponse.sendError(429, "Rate Limit Exceeded");
            return;
        }

        // Stage 3: Cache body so it can be read multiple times
        CachedBodyHttpServletRequestWrapper wrappedRequest;
        try {
            wrappedRequest = new CachedBodyHttpServletRequestWrapper(httpServletRequest);
        } catch (IOException e) {
            log.error("Failed to cache request body from IP {}: {}", ip, e.getMessage());
            httpServletResponse.sendError(400, "Bad Request");
            return;
        }

        // Stage 4: Normalize → Extract → WAF Engine
        NormalizeRequestDto normalized = normalizeService.normalize(wrappedRequest);
        ExtractRequestDto extractedRequest = extractService.extract(normalized);
        WafResult wafResult = wafEngine.evaluate(extractedRequest);

        // Stage 5: Audit log every decision
        auditLogger.log(extractedRequest, wafResult);

        if (wafResult.isBlocked()) {
            // Only record violation for fresh blocks — not cached ones
            if (!wafResult.isFromCache()) {
                banService.recordViolationVerdict(ip, wafResult.getStatusCode(), wafResult.getErrorMessage());
            }
            httpServletResponse.sendError(wafResult.getStatusCode(), wafResult.getErrorMessage());
            return;
        }

        applyRateLimitHeaders(httpServletResponse, ip);
        filterChain.doFilter(wrappedRequest, httpServletResponse);
    }

    private void applyRateLimitHeaders(HttpServletResponse response, String ip) {
        HttpHeaders headers = new HttpHeaders();
        rateLimitService.applyRateLimitHeaders(headers, ip);
        headers.forEach((key, values) ->
            values.forEach(value -> response.setHeader(key, value))
        );
    }
}