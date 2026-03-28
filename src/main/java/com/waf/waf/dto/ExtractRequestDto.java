package com.waf.waf.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractRequestDto {
    private String method;
    private String path;
    private String query;
    private String parameters;
    private String headers;
    private String body;
    private String ip;
    private String fullPayload;
    private long payloadLength;
    private Set<String> tokens;

     public String hash() {
        try {
            String combined = ip + "|" + method + "|" + path + "|" + query + "|" + body;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
