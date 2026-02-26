package com.ctasmokers.common.config.properties;

import lombok.Data;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@NullMarked
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    List<String> allowedOrigins;
    List<String> allowedMethods;
    List<String> allowedHeaders;
}
