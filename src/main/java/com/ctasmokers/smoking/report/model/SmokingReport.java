package com.ctasmokers.smoking.report.model;

import com.ctasmokers.smoking.common.model.TrainLine;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Builder
@DynamoDbImmutable(builder = SmokingReport.SmokingReportBuilder.class)
@NullMarked
public record SmokingReport(
    @DynamoDbPartitionKey
    String carNumberLine,

    String carNumber,

    TrainLine line,

    @DynamoDbSecondaryPartitionKey(indexNames = REPORT_DATE_EXPIRES_AT_INDEX)
    LocalDate reportDate,

    Instant reportedAt,

    @DynamoDbSecondarySortKey(indexNames = REPORT_DATE_EXPIRES_AT_INDEX)
    long expiresAt,

    String destinationId,

    String nextStationId,

    @Nullable
    String runNumber
) {
    public static final String REPORT_DATE_EXPIRES_AT_INDEX = "reportDate-expiresAt-index";

    public SmokingReport {
        Objects.requireNonNull(carNumberLine);
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);
        Objects.requireNonNull(reportDate);
        Objects.requireNonNull(reportedAt);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);
    }
}
