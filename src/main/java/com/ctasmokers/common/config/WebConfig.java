package com.ctasmokers.common.config;

import com.ctasmokers.common.config.properties.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@EnableWebMvc
public final class WebConfig implements WebMvcConfigurer {
    private final CorsProperties corsProperties;

    @Autowired
    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = this.corsProperties.getAllowedOrigins()
                                                     .toArray(String[]::new);
        String[] allowedMethods = this.corsProperties.getAllowedMethods()
                                                     .toArray(String[]::new);
        String[] allowedHeaders = this.corsProperties.getAllowedHeaders()
                                                     .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(false)
                .maxAge(3600);
    }
}
