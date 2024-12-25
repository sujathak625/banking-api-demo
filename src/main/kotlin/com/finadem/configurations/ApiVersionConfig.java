package com.finadem.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiVersionConfig {
    @Value("${api.version}")
    private String version;

    public String getVersion() {
        return version;
    }
}

