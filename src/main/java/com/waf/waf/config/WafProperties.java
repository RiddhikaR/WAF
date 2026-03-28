package com.waf.waf.config;


import org.springframework.boot.context.properties.ConfigurationProperties;


import lombok.Data;

@Data

@ConfigurationProperties(prefix = "waf")
public class WafProperties {

    private RateLimit rateLimit = new RateLimit();
    private Ban ban = new Ban();
    private Engine engine = new Engine();

    @Data
    public static class RateLimit {
        private int threshold = 50;
        private int windowSeconds = 60;
    }

    @Data
    public static class Ban {
        private int maxViolations = 5;
        private int banDurationMinutes = 15;
        private int violationWindowMinutes = 5;
    }

    @Data
    public static class Engine {
        private int verdictTtlMinutes = 5;
        private int replayTtlSeconds = 30;
        private long maxPayloadLength = 10000;
    }
}