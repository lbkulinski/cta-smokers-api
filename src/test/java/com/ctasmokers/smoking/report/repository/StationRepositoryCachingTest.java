package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.common.config.CacheConfig;
import com.ctasmokers.smoking.report.model.Station;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(StationRepositoryCachingTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StationRepositoryCachingTest {
    @Configuration
    @Import({CacheConfig.class, StationRepository.class})
    static class TestConfig {
        @Bean
        @SuppressWarnings("unchecked")
        DynamoDbTable<Station> stationsTable() {
            return Mockito.mock(DynamoDbTable.class);
        }

        @Bean
        DynamoDbEnhancedClient dynamoDbClient(DynamoDbTable<Station> stationsTable) {
            var client = Mockito.mock(DynamoDbEnhancedClient.class);

            when(client.table(any(String.class), any(TableSchema.class))).thenReturn(stationsTable);

            return client;
        }

        @Bean
        DynamoDbProperties dynamoDbProperties() {
            return new DynamoDbProperties("stations", "smoking-reports", "smoking-report-aggregates");
        }
    }

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private DynamoDbTable<Station> stationsTable;

    private void stubQuery(List<Station> items) {
        var page = Page.builder(Station.class)
                       .items(items)
                       .build();

        PageIterable<Station> results = PageIterable.create(() -> List.of(page).iterator());

        reset(stationsTable);
        when(stationsTable.query(any(QueryEnhancedRequest.class))).thenReturn(results);
    }

    @Test
    void existsByMapId_existingStationCalledTwice_queriesOnce() {
        stubQuery(List.of(new Station("40900", "30174")));

        assertThat(stationRepository.existsByMapId("40900")).isTrue();
        assertThat(stationRepository.existsByMapId("40900")).isTrue();

        verify(stationsTable, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void existsByMapId_differentMapIds_queriesForEach() {
        stubQuery(List.of(new Station("40900", "30174")));

        stationRepository.existsByMapId("40900");
        stationRepository.existsByMapId("41220");

        verify(stationsTable, times(2)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void existsByMapId_unknownStationCalledTwice_isNotCached() {
        stubQuery(List.of());

        assertThat(stationRepository.existsByMapId("99999")).isFalse();
        assertThat(stationRepository.existsByMapId("99999")).isFalse();

        verify(stationsTable, times(2)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void existsByMapId_stationAddedAfterMiss_isFoundOnNextCall() {
        stubQuery(List.of());

        assertThat(stationRepository.existsByMapId("40900")).isFalse();

        stubQuery(List.of(new Station("40900", "30174")));

        assertThat(stationRepository.existsByMapId("40900")).isTrue();
    }
}