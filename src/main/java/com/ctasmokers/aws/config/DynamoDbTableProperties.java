package com.ctasmokers.aws.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.aws.dynamodb.tables")
@Validated
@NullMarked
public record DynamoDbTableProperties(
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.\\-]+") String smokingReports,
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.\\-]+") String smokingReportAggregates
) {}
