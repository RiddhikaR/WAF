package com.waf.waf.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NormalizeRequestDto {
    private String method;
    private String path;
    private String query;
    private String parameters;
    private String headers;
    private String body;
    private String ip;
}
