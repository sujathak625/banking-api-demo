package com.finadem.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApiVersionConfig {
    @Value("${api.version}")
    private String version;
}

