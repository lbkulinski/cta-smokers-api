package com.ctasmokers.smoking.aggregate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.NullMarked;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Value
@Builder
@NullMarked
@DynamoDbImmutable(builder = SmokingReportAggregate.SmokingReportAggregateBuilder.class)
public class SmokingReportAggregate {
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    String pk;

    @Getter(onMethod_ = @DynamoDbSortKey)
    String sk;

    long reportCount;
}
