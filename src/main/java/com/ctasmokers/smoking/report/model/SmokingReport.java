package com.ctasmokers.smoking.report.model;

import com.ctasmokers.smoking.common.model.TrainLine;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@DynamoDbImmutable(builder = SmokingReport.SmokingReportBuilder.class)
@NullMarked
public record SmokingReport(
    @DynamoDbPartitionKey
    LocalDate date,

    @DynamoDbSortKey
    String reportId,

    Instant reportedAt,

    @DynamoDbSecondarySortKey(indexNames = CAR_NUMBER_LINE_EXPIRES_AT_INDEX)
    long expiresAt,

    TrainLine line,

    String destinationId,

    String nextStationId,

    String carNumber,

    @DynamoDbSecondaryPartitionKey(indexNames = CAR_NUMBER_LINE_EXPIRES_AT_INDEX)
    String carNumberLine,

    @Nullable
    String runNumber
) {
    public static final String CAR_NUMBER_LINE_EXPIRES_AT_INDEX = "carNumberLine-expiresAt-index";
}
