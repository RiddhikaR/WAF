package com.waf.waf.service;



import org.springframework.stereotype.Service;

import com.waf.waf.dto.ExtractRequestDto;
import com.waf.waf.dto.NormalizeRequestDto;

@Service
public interface ExtractService {

    ExtractRequestDto extract(NormalizeRequestDto normalizeRequestDto);

    
}
