package com.waf.waf.engine.rules;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class PathTraversalRule implements WafRule {

    @Override
    public WafResult check(ExtractRequestDto request) {
        // FIX: Use fullPayload directly instead of request.toString()
        String input = request.getFullPayload();
        if (input == null) return WafResult.allow();

        if (input.contains("../") || input.contains("..\\")) {
            return WafResult.block(403, "Path Traversal Detected");
        }

        return WafResult.allow();
    }
}