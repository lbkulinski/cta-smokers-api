package com.ctasmokers.smoking.report.model;

import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.Objects;

@Builder
@DynamoDbImmutable(builder = Station.StationBuilder.class)
@NullMarked
public record Station(
    @DynamoDbPartitionKey String mapId,
    @DynamoDbSortKey String stationId
) {
    public Station {
        Objects.requireNonNull(mapId);
        Objects.requireNonNull(stationId);
    }
}
