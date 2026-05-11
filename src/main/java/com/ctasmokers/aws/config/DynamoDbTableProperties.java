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
    @NotBlank
    @Pattern(
        regexp = TABLE_NAME_PATTERN,
        message = TABLE_NAME_PATTERN_MESSAGE
    )
    String smokingReports,

    @NotBlank
    @Pattern(
        regexp = TABLE_NAME_PATTERN,
        message = TABLE_NAME_PATTERN_MESSAGE
    ) String smokingReportAggregates
) {
    private static final String TABLE_NAME_PATTERN = "^[a-zA-Z0-9_.\\-]{3,255}$";
    private static final String TABLE_NAME_PATTERN_MESSAGE = """
    Must contain only letters, numbers, underscores, hyphens, or dots and be between 3 and 255 characters long""";
}
