package com.ctasmokers.aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws.dynamodb.tables")
public record DynamoDbTableProperties(
    String smokingReports,
    String smokingReportAggregates
) {
}
