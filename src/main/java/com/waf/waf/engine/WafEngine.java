package com.waf.waf.engine;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.waf.waf.config.WafProperties;
import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.WafResult;
import com.waf.waf.engine.cache.ReplayAttackCache;
import com.waf.waf.engine.cache.VerdictCache;
import com.waf.waf.engine.rules.WafRule;
import com.waf.waf.engine.rules.WafRules;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WafEngine {

    private final VerdictCache verdictCache;
    private final ReplayAttackCache replayAttackCache;
    private final WafProperties wafProperties;

    // Only apply replay detection to state-changing methods
    private static final Set<String> REPLAY_CHECKED_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    public WafResult evaluate(ExtractRequestDto request) {
        String requestHash = request.hash();

        // Use config-driven TTLs
        Duration verdictTtl = Duration.ofMinutes(wafProperties.getEngine().getVerdictTtlMinutes());
        Duration replayTtl = Duration.ofSeconds(wafProperties.getEngine().getReplayTtlSeconds());

        // Replay check — only for state-changing methods
        if (REPLAY_CHECKED_METHODS.contains(request.getMethod())) {
            if (replayAttackCache.isReplay(requestHash)) {
                return WafResult.block(429, "Replay Attack Detected");
            }
            replayAttackCache.record(requestHash, replayTtl);
        }

        // Verdict cache check
        WafResult cached = verdictCache.get(requestHash);
        if (cached != null) {
            return cached;
        }

        // Evaluate rules — config-driven max payload length
        List<WafRule> rules = WafRules.getRules(wafProperties.getEngine().getMaxPayloadLength());
        WafResult result = WafResult.allow();
        for (WafRule rule : rules) {
            WafResult r = rule.check(request);
            if (r.isBlocked()) {
                result = r;
                break;
            }
        }

        verdictCache.put(requestHash, result, verdictTtl);
        return result;
    }
}