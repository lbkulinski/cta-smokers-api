package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.report.model.Station;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Objects;

@Repository
@NullMarked
public class StationRepository {
    private final DynamoDbTable<@Nullable Station> stations;

    @Autowired
    public StationRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        DynamoDbProperties dynamoDbProperties
    ) {
        String tableName = dynamoDbProperties.stations();
        var tableSchema = TableSchema.fromImmutableClass(Station.class);

        this.stations = dynamoDbClient.table(tableName, tableSchema);
    }

    @Cacheable(value = "stationMapIds", unless = "!#result")
    public boolean existsByMapId(String mapId) {
        Objects.requireNonNull(mapId);

        var key = Key.builder()
                     .partitionValue(mapId)
                     .build();

        var queryConditional = QueryConditional.keyEqualTo(key);

        var request = QueryEnhancedRequest.builder()
                                          .queryConditional(queryConditional)
                                          .limit(1)
                                          .build();

        return this.stations.query(request)
                            .items()
                            .stream()
                            .findAny()
                            .isPresent();
    }
}
