package com.waf.waf.engine.rules;

import java.util.regex.Pattern;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class XssRule implements WafRule {

    // FIX: Extended pattern to catch more XSS variants including encoded forms
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script[\\s>]" +
        "|</script>" +
        "|javascript\\s*:" +
        "|on(error|load|click|mouseover|focus|blur|change|submit|reset|keydown|keyup)\\s*=" +
        "|eval\\s*\\(" +
        "|document\\s*\\.\\s*cookie" +
        "|alert\\s*\\(" +
        "|<iframe[\\s>]" +
        "|<img[^>]+src\\s*=\\s*['\"]?javascript" +
        "|expression\\s*\\(",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public WafResult check(ExtractRequestDto request) {
        // FIX: Use fullPayload directly instead of request.toString()
        String input = request.getFullPayload();
        if (input == null) return WafResult.allow();

        if (XSS_PATTERN.matcher(input).find()) {
            return WafResult.block(403, "XSS Attack Detected");
        }

        return WafResult.allow();
    }
}