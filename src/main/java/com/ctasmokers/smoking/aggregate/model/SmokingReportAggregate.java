package com.ctasmokers.smoking.aggregate.model;

import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.Objects;

@Builder
@DynamoDbImmutable(builder = SmokingReportAggregate.SmokingReportAggregateBuilder.class)
@NullMarked
public record SmokingReportAggregate(
    @DynamoDbPartitionKey
    @DynamoDbAttribute("PK")
    String pk,

    @DynamoDbSortKey
    @DynamoDbAttribute("SK")
    String sk,

    long reportCount
) {
    public SmokingReportAggregate {
        Objects.requireNonNull(pk);
        Objects.requireNonNull(sk);
    }
}
