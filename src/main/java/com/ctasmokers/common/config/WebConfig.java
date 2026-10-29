package com.ctasmokers.common.config;

import com.ctasmokers.common.config.properties.CorsProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CorsProperties corsProperties;

    @Autowired
    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        var allowedOrigins = this.corsProperties.allowedOrigins().toArray(String[]::new);
        var allowedMethods = this.corsProperties.allowedMethods().toArray(String[]::new);
        var allowedHeaders = this.corsProperties.allowedHeaders().toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders);
    }
}
