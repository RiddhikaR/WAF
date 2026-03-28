package com.waf.waf.engine.rules;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;

public interface WafRule {
    WafResult check(ExtractRequestDto request);
}