package com.waf.waf.engine.rules;

import java.util.regex.Pattern;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class OsCommandInjectionRule implements WafRule {

    // FIX: Use || instead of | alone (too broad), add more command patterns
    private static final Pattern OS_PATTERN = Pattern.compile(
        "&&|\\|\\||;\\s*(ls|cat|rm|wget|curl|bash|sh|python|perl|nc|ncat|chmod|chown|whoami|id|uname)" +
        "|`[^`]+`" +
        "|\\$\\([^)]+\\)" +
        "|\\brm\\s+-rf\\b" +
        "|\\bbash\\b|\\b/bin/sh\\b",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public WafResult check(ExtractRequestDto request) {
        // FIX: Use fullPayload directly instead of request.toString()
        String input = request.getFullPayload();
        if (input == null) return WafResult.allow();

        if (OS_PATTERN.matcher(input).find()) {
            return WafResult.block(403, "OS Command Injection Detected");
        }

        return WafResult.allow();
    }
}