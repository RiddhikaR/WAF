package com.waf.waf.serviceImpl;

import java.io.BufferedReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

import org.springframework.stereotype.Service;

import com.waf.waf.dto.NormalizeRequestDto;
import com.waf.waf.service.NormalizeService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class NormalizeServiceImpl implements NormalizeService {

    private static final int MAX_ROUNDS = 3;

    @Override
    public NormalizeRequestDto normalize(HttpServletRequest request) {
        return NormalizeRequestDto.builder()
                .method(normalizeMethod(request))
                .path(normalizePath(request.getRequestURI()))
                .query(normalizeQuery(request.getQueryString()))
                .parameters(normalizeParameters(request))
                .headers(normalizeHeaders(request))
                .body(normalizeBody(request))
                 .ip(request.getRemoteAddr()) 

                .build();
    }

    public String normalizeMethod(HttpServletRequest request) {
        return request.getMethod() == null ? null : request.getMethod().toUpperCase();
    }

    public String normalizePath(String path) {
        if (path == null) return null;

        String normalized = unicode(decode(path));
        normalized = normalized.replace('\\', '/');
        normalized = normalized.replaceAll("/+", "/");

        try {
            return new URI(normalized).normalize().getPath();
        } catch (Exception e) {
            return normalized;
        }
    }

    public String normalizeQuery(String query) {
        if (query == null) return null;

        String q = unicode(decode(query));
        q = q.replaceAll("[;&]+", "&");

        String[] pairs = q.split("&");
        Arrays.sort(pairs);

        return String.join("&", pairs);
    }

    public String normalizeParameters(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        if (params == null || params.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();

        params.keySet().stream().sorted().forEach(key -> {
            String keyNorm = unicode(decode(key));
            String[] values = params.get(key);
            Arrays.sort(values);

            for (String val : values) {
                sb.append(keyNorm)
                        .append("=")
                        .append(unicode(decode(val)))
                        .append("&");
            }
        });

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    // ✅ Normalize Headers
    public String normalizeHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) return null;

        Map<String, String> headers = new TreeMap<>();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);

            String keyNorm = unicode(decode(name)).toLowerCase();
            String valNorm = unicode(decode(value));

            headers.put(keyNorm, valNorm);
        }

        StringBuilder sb = new StringBuilder();
        headers.forEach((k, v) -> sb.append(k).append(":").append(v).append("\n"));

        return sb.toString().trim();
    }

    // ✅ Normalize Body
    public String normalizeBody(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            if (body.length() == 0) return null;

            return unicode(decode(body.toString()));
        } catch (Exception e) {
            return null;
        }
    }

    // ✅ URL Decode (multiple rounds, capped)
    private String decode(String input) {
        if (input == null) return null;

        String decoded = input;
        for (int i = 0; i < MAX_ROUNDS; i++) {
            try {
                String temp = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
                if (temp.equals(decoded)) break;
                decoded = temp;
            } catch (Exception e) {
                break;
            }
        }
        return decoded;
    }

    // ✅ Unicode Normalization (prevents evasion attacks)
    private String unicode(String input) {
        if (input == null) return null;
        return Normalizer.normalize(input, Normalizer.Form.NFKC);
    }
}
