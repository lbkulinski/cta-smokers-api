package com.ctasmokers.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
@Validated
@NullMarked
public record CorsProperties(
    @NotEmpty List<@NotBlank String> allowedOrigins,
    @NotEmpty List<@NotBlank String> allowedMethods,
    @NotEmpty List<@NotBlank String> allowedHeaders
) {}
