package com.ctasmokers.smoking.report.model;

import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@DynamoDbImmutable(builder = SmokingReport.SmokingReportBuilder.class)
@NullMarked
public record SmokingReport(
    @DynamoDbPartitionKey LocalDate date,
    @DynamoDbSortKey String reportId,
    Instant reportedAt,
    long expiresAt,
    TrainLine line,
    String destinationId,
    String nextStationId,
    String carNumber,
    @Nullable String runNumber
) {}
