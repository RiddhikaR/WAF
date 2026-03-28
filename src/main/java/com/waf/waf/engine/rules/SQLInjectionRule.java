package com.waf.waf.engine.rules;

import java.util.regex.Pattern;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class SQLInjectionRule implements WafRule {

    // FIX: Use regex with word boundaries to avoid false positives on normal English
    private static final Pattern SQL_PATTERN = Pattern.compile(
        "\\b(select|insert|update|delete|drop|union|truncate)\\b.{0,50}\\b(from|into|table|where|set)\\b" +
        "|'\\s*(or|and)\\s*'?\\d+'?\\s*=\\s*'?\\d+'?" +
        "|--\\s*$" +
        "|;\\s*(drop|delete|insert|update)\\b",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public WafResult check(ExtractRequestDto request) {
        // FIX: Use fullPayload directly instead of request.toString()
        String input = request.getFullPayload();
        if (input == null) return WafResult.allow();

        if (SQL_PATTERN.matcher(input).find()) {
            return WafResult.block(403, "SQL Injection Detected");
        }

        return WafResult.allow();
    }
}