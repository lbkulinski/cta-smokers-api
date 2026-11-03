package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.report.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationRepositoryTest {
    private static final String TABLE_NAME = "stations";

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbTable stations;

    @Mock
    private DynamoDbProperties dynamoDbProperties;

    private StationRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(dynamoDbProperties.stations()).thenReturn(TABLE_NAME);
        when(dynamoDbClient.table(eq(TABLE_NAME), any(TableSchema.class))).thenReturn(stations);

        repository = new StationRepository(dynamoDbClient, dynamoDbProperties);
    }

    private static Page<Station> page(List<Station> items) {
        return Page.builder(Station.class)
                   .items(items)
                   .build();
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(Page<Station>... pages) {
        PageIterable<Station> results = PageIterable.create(() -> List.of(pages).iterator());

        when(stations.query(any(QueryEnhancedRequest.class))).thenReturn(results);
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByMapId_stationFound_returnsTrue() {
        stubQuery(page(List.of(new Station("40900", "30174"))));

        assertThat(repository.existsByMapId("40900")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByMapId_emptyPage_returnsFalse() {
        stubQuery(page(List.of()));

        assertThat(repository.existsByMapId("99999")).isFalse();
    }

    @Test
    void existsByMapId_noPages_returnsFalse() {
        stubQuery();

        assertThat(repository.existsByMapId("99999")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsByMapId_queriesMapIdPartitionWithLimitOne() {
        stubQuery(page(List.of(new Station("40900", "30174"))));

        repository.existsByMapId("40900");

        ArgumentCaptor<QueryEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(stations).query(requestCaptor.capture());

        QueryEnhancedRequest request = requestCaptor.getValue();
        assertThat(request.limit()).isEqualTo(1);

        TableSchema<Station> tableSchema = TableSchema.fromImmutableClass(Station.class);
        Expression expression = request.queryConditional()
                                       .expression(tableSchema, TableMetadata.primaryIndexName());

        assertThat(expression.expressionValues().values())
            .extracting(AttributeValue::s)
            .containsExactly("40900");
    }

    @Test
    void existsByMapId_nullMapId_throwsNullPointer() {
        assertThatThrownBy(() -> repository.existsByMapId(null))
            .isInstanceOf(NullPointerException.class);
    }
}