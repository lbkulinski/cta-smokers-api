package com.ctasmokers.smoking.report.model;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StationTest {
    @Test
    void tableSchema_usesMapIdPartitionKeyAndStopIdSortKey() {
        var metadata = TableSchema.fromImmutableClass(Station.class)
                                  .tableMetadata();

        assertThat(metadata.primaryPartitionKey()).isEqualTo("mapId");
        assertThat(metadata.primarySortKey()).hasValue("stopId");
    }

    @Test
    void builder_buildsStation() {
        Station station = Station.builder()
                                 .mapId("40900")
                                 .stopId("30174")
                                 .build();

        assertThat(station).isEqualTo(new Station("40900", "30174"));
    }

    @Test
    void constructor_nullMapId_throws() {
        assertThatThrownBy(() -> new Station(null, "30174"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullStopId_throws() {
        assertThatThrownBy(() -> new Station("40900", null))
            .isInstanceOf(NullPointerException.class);
    }
}