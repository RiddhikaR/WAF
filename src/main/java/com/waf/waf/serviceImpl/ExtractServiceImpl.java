package com.waf.waf.serviceImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.NormalizeRequestDto;
import com.waf.waf.service.ExtractService;

@Service
public class ExtractServiceImpl implements ExtractService {
    @Override
    public ExtractRequestDto extract(NormalizeRequestDto normalizeRequestDto) {

        String fullPayload = join(
                normalizeRequestDto.getPath(),
                normalizeRequestDto.getQuery(),
                normalizeRequestDto.getParameters(),
                normalizeRequestDto.getHeaders(),
                normalizeRequestDto.getBody()
        );
        Set<String> tokens=tokenize(fullPayload);
        return ExtractRequestDto.builder()
        .method(normalizeRequestDto.getMethod())
        .path(normalizeRequestDto.getPath())
        .query(normalizeRequestDto.getQuery())
        .parameters(normalizeRequestDto.getParameters())
        .headers(normalizeRequestDto.getHeaders())
        .body(normalizeRequestDto.getBody())
        .ip(normalizeRequestDto.getIp()) // ← add this
        .fullPayload(fullPayload)
        .payloadLength(fullPayload.length())
        .tokens(tokens)
        .build();
    }

    private String join(String path,
                        String query,
                        String parameters,
                        String headers,
                        String body) {

        StringBuilder sb = new StringBuilder();

        append(sb, path);
        append(sb, query);
        append(sb, parameters);
        append(sb, headers);
        append(sb, body);

        return sb.toString().trim();
    }

    private void append(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(value).append(' ');
        }
    }
    private Set<String> tokenize(String input) {
        if (input == null || input.isBlank()) {
            return Set.of();
        }

        return new HashSet<>(
                Arrays.asList(input.split("[^a-zA-Z0-9_]+"))
        );
    }
}
