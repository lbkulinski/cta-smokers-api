package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
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
    private static final String REPORT_ID_KEY = "reportId";
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

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbIndex carNumberLineIndex;

    @Mock
    @SuppressWarnings("rawtypes")
    private SdkIterable indexResults;

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
                            .line(TrainLine.RED)
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

        SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.reports()).containsExactly(report);
        assertThat(result.nextCursor()).isNull();
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
        assertThat(startKey.get(REPORT_ID_KEY).s()).isEqualTo(REPORT_ID);
    }

    @Test
    void findPageByDate_withNextPage_returnsLastEvaluatedKey() {
        Map<String, AttributeValue> lastKey = Map.of(
            REPORT_ID_KEY, AttributeValue.builder().s(REPORT_ID).build()
        );

        when(smokingReports.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(lastKey);

        SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.nextCursor()).isEqualTo(REPORT_ID);
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

        SmokingReportPage result = repository.findPageByDate(DATE, 10, null);

        assertThat(result.reports()).isEmpty();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void existsActiveByCarNumberAndLine_activeReportExists_returnsTrue() {
        when(smokingReports.index(SmokingReport.CAR_NUMBER_LINE_INDEX)).thenReturn(carNumberLineIndex);
        when(carNumberLineIndex.query(any(QueryEnhancedRequest.class))).thenReturn(indexResults);
        when(indexResults.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of(report()));

        boolean result = repository.existsActiveByCarNumberAndLine("2435", TrainLine.RED);

        assertThat(result).isTrue();

        ArgumentCaptor<QueryEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(carNumberLineIndex).query(requestCaptor.capture());

        QueryEnhancedRequest request = requestCaptor.getValue();
        Key expectedKey = Key.builder()
                             .partitionValue("2435")
                             .sortValue("RED")
                             .build();

        assertThat(request.queryConditional()).isEqualTo(QueryConditional.keyEqualTo(expectedKey));
        assertThat(request.limit()).isEqualTo(1);
        assertThat(request.filterExpression().expression()).isEqualTo("expiresAt > :now");
        assertThat(request.filterExpression().expressionValues()).containsKey(":now");
    }

    @Test
    void existsActiveByCarNumberAndLine_noActiveReport_returnsFalse() {
        when(smokingReports.index(SmokingReport.CAR_NUMBER_LINE_INDEX)).thenReturn(carNumberLineIndex);
        when(carNumberLineIndex.query(any(QueryEnhancedRequest.class))).thenReturn(indexResults);
        when(indexResults.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of());

        boolean result = repository.existsActiveByCarNumberAndLine("2435", TrainLine.RED);

        assertThat(result).isFalse();
    }

    @Test
    void existsActiveByCarNumberAndLine_noResults_returnsFalse() {
        when(smokingReports.index(SmokingReport.CAR_NUMBER_LINE_INDEX)).thenReturn(carNumberLineIndex);
        when(carNumberLineIndex.query(any(QueryEnhancedRequest.class))).thenReturn(indexResults);
        when(indexResults.stream()).thenReturn(Stream.empty());

        boolean result = repository.existsActiveByCarNumberAndLine("2435", TrainLine.RED);

        assertThat(result).isFalse();
    }
}