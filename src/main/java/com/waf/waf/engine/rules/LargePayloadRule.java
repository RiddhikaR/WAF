package com.waf.waf.engine.rules;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public class LargePayloadRule implements WafRule {

    private static final long MAX_PAYLOAD_LENGTH = 10_000; // 10KB default, overridden via WafProperties

    private final long maxPayloadLength;

    public LargePayloadRule(long maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public LargePayloadRule() {
        this.maxPayloadLength = MAX_PAYLOAD_LENGTH;
    }

    @Override
    public WafResult check(ExtractRequestDto request) {
        if (request.getPayloadLength() > maxPayloadLength) {
            return WafResult.block(413, "Payload Too Large");
        }
        return WafResult.allow();
    }
}