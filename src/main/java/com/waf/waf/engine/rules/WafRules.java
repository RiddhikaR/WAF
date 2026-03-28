package com.waf.waf.engine.rules;

import java.util.List;

public class WafRules {

    private static final List<WafRule> RULES = List.of(
        new SQLInjectionRule(),
        new XssRule(),
        new PathTraversalRule(),
        new OsCommandInjectionRule(),
        new CRLFRule()
    );

    // Default rules (no config)
    public static List<WafRule> getRules() {
        return RULES;
    }

    // Config-aware rules (includes LargePayloadRule with configured limit)
    public static List<WafRule> getRules(long maxPayloadLength) {
        return List.of(
            new SQLInjectionRule(),
            new XssRule(),
            new PathTraversalRule(),
            new OsCommandInjectionRule(),
            new CRLFRule(),
            new LargePayloadRule(maxPayloadLength)
        );
    }
}