package com.ctasmokers.smoking.report.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.cta.reports")
@Validated
@NullMarked
public record CtaReportProperties(
    @NotBlank
    String baseUrl,

    @Min(1)
    @Max(100)
    int pageSize,

    @Positive
    long expireAfterMinutes
) {}
