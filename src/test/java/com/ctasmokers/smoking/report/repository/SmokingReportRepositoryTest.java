package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.smoking.report.model.SmokingReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportRepositoryTest {
    private static final String TABLE_NAME = "smoking-reports";
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);
    private static final String REPORT_ID = "1234567890_abc-def";

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbTable smokingReports;

    @Mock
    @SuppressWarnings("rawtypes")
    private PageIterable pageIterable;

    @Mock
    @SuppressWarnings("rawtypes")
    private Page page;

    private SmokingReportRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(dynamoDbClient.table(eq(TABLE_NAME), any())).thenReturn(smokingReports);

        repository = new SmokingReportRepository(dynamoDbClient, TABLE_NAME);
    }

    private SmokingReport report() {
        return SmokingReport.builder()
                            .date(DATE)
                            .reportId(REPORT_ID)
                            .reportedAt(Instant.now())
                            .expiresAt(Instant.now().getEpochSecond() + 3600)
                            .line(com.ctasmokers.smoking.common.model.TrainLine.RED)
                            .destinationId("40900")
                            .nextStationId("41220")
                            .carNumber("2435")
                            .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_putsItem() {
        SmokingReport report = report();

        repository.save(report);

        verify(smokingReports).putItem(report);
    }

    @Test
    void findById_found() {
        SmokingReport report = report();

        when(smokingReports.getItem(any(Key.class))).thenReturn(report);

        Optional<SmokingReport> result = repository.findById(DATE, REPORT_ID);

        assertThat(result).contains(report);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReports).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(DATE.toString());
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo(REPORT_ID);
    }

    @Test
    void findById_notFound() {
        when(smokingReports.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReport> result = repository.findById(DATE, REPORT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findPageByDate_returnsReports() {
        SmokingReport report = report();

        when(smokingReports.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of(report));
        when(page.lastEvaluatedKey()).thenReturn(null);

        SmokingReportRepository.SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.reports()).containsExactly(report);
        assertThat(result.lastEvaluatedKey()).isNull();
    }

    @Test
    void findPageByDate_withCursor_setsExclusiveStartKey() {
        when(smokingReports.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(null);

        repository.findPageByDate(DATE, 10, REPORT_ID);

        ArgumentCaptor<QueryEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(smokingReports).query(requestCaptor.capture());
        Map<String, AttributeValue> startKey = requestCaptor.getValue().exclusiveStartKey();
        assertThat(startKey).isNotNull();
        assertThat(startKey.get(SmokingReportRepository.REPORT_ID_KEY).s()).isEqualTo(REPORT_ID);
    }

    @Test
    void findPageByDate_withNextPage_returnsLastEvaluatedKey() {
        Map<String, AttributeValue> lastKey = Map.of(
            SmokingReportRepository.REPORT_ID_KEY, AttributeValue.builder().s(REPORT_ID).build()
        );

        when(smokingReports.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(lastKey);

        SmokingReportRepository.SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.lastEvaluatedKey()).isEqualTo(lastKey);
    }

    @Test
    void findPageByDate_invalidPageSize_throws() {
        assertThatThrownBy(() -> repository.findPageByDate(DATE, 0, null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> repository.findPageByDate(DATE, 101, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findPageByDate_noResults_returnsEmptyPage() {
        when(smokingReports.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.empty());

        SmokingReportRepository.SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.reports()).isEmpty();
        assertThat(result.lastEvaluatedKey()).isNull();
    }
}
