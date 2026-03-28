package com.waf.waf.service;

import com.waf.waf.dto.NormalizeRequestDto;


import jakarta.servlet.http.HttpServletRequest;

public interface NormalizeService {
     NormalizeRequestDto normalize(HttpServletRequest request);
}
