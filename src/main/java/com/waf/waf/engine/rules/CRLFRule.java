package com.waf.waf.engine.rules;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class CRLFRule implements WafRule {

    @Override
    public WafResult check(ExtractRequestDto request) {
        // FIX: Do NOT check headers — normalizeHeaders() joins them with \n intentionally.
        // Only check path, query, and body for injected CRLF characters.
        String input = join(request.getPath(), request.getQuery(), request.getBody());

        if (input.contains("\r") || input.contains("\n")) {
            return WafResult.block(403, "CRLF Injection Detected");
        }

        return WafResult.allow();
    }

    private String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) sb.append(part);
        }
        return sb.toString();
    }
}