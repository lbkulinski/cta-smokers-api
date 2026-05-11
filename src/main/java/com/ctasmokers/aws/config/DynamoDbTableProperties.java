package com.ctasmokers.aws.config;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.aws.dynamodb.tables")
@Validated
@NullMarked
public record DynamoDbTableProperties(
    @NotBlank String smokingReports,
    @NotBlank String smokingReportAggregates
) {}
