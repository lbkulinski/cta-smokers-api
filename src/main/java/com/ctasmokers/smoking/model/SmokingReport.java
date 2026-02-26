package com.ctasmokers.smoking.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder
@NullMarked
@DynamoDbImmutable(builder = SmokingReport.SmokingReportBuilder.class)
public class SmokingReport {
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    LocalDate date;

    @Getter(onMethod_ = @DynamoDbSortKey)
    String reportId;

    Instant reportedAt;

    long expiresAt;

    TrainLine line;

    String destinationId;

    String nextStationId;

    String carNumber;

    @Nullable
    String runNumber;
}
