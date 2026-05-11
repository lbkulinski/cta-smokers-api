package com.ctasmokers.aws.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.aws.dynamodb.tables")
@Validated
@NullMarked
public record DynamoDbTableProperties(
    @NotBlank
    @Size(min = 3, max = 255)
    @Pattern(
        regexp = "[a-zA-Z0-9_.\\-]+",
        message = "must contain only letters, numbers, underscores, hyphens, or dots"
    )
    String smokingReports,

    @NotBlank
    @Size(min = 3, max = 255)
    @Pattern(
        regexp = "[a-zA-Z0-9_.\\-]+",
        message = "must contain only letters, numbers, underscores, hyphens, or dots"
    ) String smokingReportAggregates
) {}
