package com.ctasmokers.smoking.report.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cta.reports")
@NullMarked
public record CtaReportsProperties(
    String baseUrl,
    int pageSize,
    long expireAfterMinutes
) {}
