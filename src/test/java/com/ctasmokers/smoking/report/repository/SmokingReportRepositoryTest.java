package com.ctasmokers.smoking.report.repository;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.report.cursor.ReportPageCursor;
import com.ctasmokers.smoking.report.cursor.SmokingReportCursorCodec;
import com.ctasmokers.smoking.report.exception.InvalidCursorException;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import com.ctasmokers.smoking.report.model.SmokingReportLimits;
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
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportRepositoryTest {
    private static final String TABLE_NAME = "smoking-reports";
    private static final String CURSOR = "c2FtcGxlLWN1cnNvcg";

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbTable smokingReports;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbIndex activeIndex;

    @Mock
    @SuppressWarnings("rawtypes")
    private SdkIterable indexResults;

    @Mock
    @SuppressWarnings("rawtypes")
    private Page page;

    @Mock
    private SmokingReportCursorCodec cursorCodec;

    @Mock
    private DynamoDbProperties dynamoDbProperties;

    private SmokingReportRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(dynamoDbProperties.smokingReports()).thenReturn(TABLE_NAME);
        when(dynamoDbClient.table(eq(TABLE_NAME), any(TableSchema.class))).thenReturn(smokingReports);

        repository = new SmokingReportRepository(dynamoDbClient, cursorCodec, dynamoDbProperties);
    }

    private SmokingReport report() {
        return SmokingReport.builder()
                            .carNumberLine("2435#RED")
                            .carNumber("2435")
                            .line(TrainLine.RED)
                            .gsiPk(SmokingReportSchema.GSI_PK_VALUE)
                            .reportedAt(Instant.now())
                            .expiresAt(Instant.now().getEpochSecond() + 3600L)
                            .destinationId("40900")
                            .nextStationId("41220")
                            .build();
    }

    private void stubIndexQuery(Stream<Page<SmokingReport>> pages) {
        when(smokingReports.index(SmokingReportSchema.GSI_PK_EXPIRES_AT_INDEX)).thenReturn(activeIndex);
        when(activeIndex.query(any(QueryEnhancedRequest.class))).thenReturn(indexResults);
        when(indexResults.stream()).thenReturn(pages);
    }

    private QueryEnhancedRequest capturedQueryRequest() {
        ArgumentCaptor<QueryEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
        verify(activeIndex).query(requestCaptor.capture());

        return requestCaptor.getValue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_putsItemWithNotExistsOrExpiredCondition() {
        SmokingReport report = report();

        long before = Instant.now().getEpochSecond();

        repository.save(report);

        long after = Instant.now().getEpochSecond();

        ArgumentCaptor<PutItemEnhancedRequest<SmokingReport>> requestCaptor =
            ArgumentCaptor.forClass(PutItemEnhancedRequest.class);
        verify(smokingReports).putItem(requestCaptor.capture());

        PutItemEnhancedRequest<SmokingReport> request = requestCaptor.getValue();
        assertThat(request.item()).isSameAs(report);

        Expression condition = request.conditionExpression();
        assertThat(condition.expression()).isEqualTo("attribute_not_exists(carNumberLine) OR expiresAt <= :now");
        assertThat(condition.expressionValues()).containsOnlyKeys(":now");

        long now = Long.parseLong(condition.expressionValues().get(":now").n());
        assertThat(now).isBetween(before, after);
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_whenConditionFails_throwsAlreadyExists() {
        doThrow(ConditionalCheckFailedException.builder().build()).when(smokingReports)
                                                                  .putItem(any(PutItemEnhancedRequest.class));

        assertThatThrownBy(() -> repository.save(report()))
            .isInstanceOf(SmokingReportAlreadyExistsException.class)
            .hasMessageContaining("2435")
            .hasMessageContaining("RED");
    }

    @Test
    void findByCarNumberLine_found_usesCompositeKey() {
        SmokingReport report = report();

        when(smokingReports.getItem(any(Key.class))).thenReturn(report);

        SmokingReport result = repository.findByCarNumberLine("2435", TrainLine.RED);

        assertThat(result).isEqualTo(report);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReports).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo("2435#RED");
        assertThat(keyCaptor.getValue().sortKeyValue()).isEmpty();
    }

    @Test
    void findByCarNumberLine_notFound_returnsNull() {
        when(smokingReports.getItem(any(Key.class))).thenReturn(null);

        assertThat(repository.findByCarNumberLine("2435", TrainLine.RED)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActivePage_returnsReportsNewestFirstWithExpiresAtCondition() {
        SmokingReport report = report();

        stubIndexQuery(Stream.of(page));
        when(page.items()).thenReturn(List.of(report));
        when(page.lastEvaluatedKey()).thenReturn(null);

        long before = Instant.now().getEpochSecond();

        SmokingReportPage result = repository.findActivePage(10, null);

        long after = Instant.now().getEpochSecond();

        assertThat(result.reports()).containsExactly(report);
        assertThat(result.nextCursor()).isNull();

        QueryEnhancedRequest request = capturedQueryRequest();
        assertThat(request.limit()).isEqualTo(10);
        assertThat(request.scanIndexForward()).isFalse();
        assertThat(request.exclusiveStartKey()).isNull();

        TableSchema<SmokingReport> tableSchema = TableSchema.fromImmutableClass(SmokingReport.class);
        Expression expression = request.queryConditional()
                                       .expression(tableSchema, SmokingReportSchema.GSI_PK_EXPIRES_AT_INDEX);

        assertThat(expression.expression()).contains(" > ");
        assertThat(expression.expressionValues().values())
            .extracting(AttributeValue::s)
            .contains(SmokingReportSchema.GSI_PK_VALUE);
        assertThat(expression.expressionValues().values())
            .filteredOn(value -> value.n() != null)
            .extracting(value -> Long.parseLong(value.n()))
            .allSatisfy(epochSecond -> assertThat(epochSecond).isBetween(before, after));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActivePage_withCursor_usesCursorKeyAsExclusiveStartKey() {
        long expiresAt = Instant.now().getEpochSecond() + 600L;
        var cursor = new ReportPageCursor("2435#RED", SmokingReportSchema.GSI_PK_VALUE, expiresAt);

        when(cursorCodec.decode(CURSOR)).thenReturn(cursor);
        stubIndexQuery(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(null);

        repository.findActivePage(10, CURSOR);

        assertThat(capturedQueryRequest().exclusiveStartKey()).isEqualTo(cursor.toKey());
    }

    @Test
    void findActivePage_cursorAlreadyExpired_returnsEmptyPageWithoutQuerying() {
        long expiresAt = Instant.now().getEpochSecond() - 1L;
        var cursor = new ReportPageCursor("2435#RED", SmokingReportSchema.GSI_PK_VALUE, expiresAt);

        when(cursorCodec.decode(CURSOR)).thenReturn(cursor);

        SmokingReportPage result = repository.findActivePage(10, CURSOR);

        assertThat(result.reports()).isEmpty();
        assertThat(result.nextCursor()).isNull();
        verify(smokingReports, never()).index(any());
    }

    @Test
    void findActivePage_invalidCursor_propagatesInvalidCursor() {
        when(cursorCodec.decode("bad")).thenThrow(new InvalidCursorException("bad"));

        assertThatThrownBy(() -> repository.findActivePage(10, "bad"))
            .isInstanceOf(InvalidCursorException.class);

        verify(smokingReports, never()).index(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActivePage_withMoreResults_encodesLastEvaluatedKeyAsCursor() {
        Map<String, AttributeValue> lastKey = Map.of("carNumberLine", AttributeValue.fromS("2435#RED"));

        stubIndexQuery(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(lastKey);
        when(cursorCodec.encode(lastKey)).thenReturn(CURSOR);

        SmokingReportPage result = repository.findActivePage(10, null);

        assertThat(result.nextCursor()).isEqualTo(CURSOR);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findActivePage_cursorEncodingFails_propagatesIllegalState() {
        Map<String, AttributeValue> lastKey = Map.of("carNumberLine", AttributeValue.fromS("2435#RED"));

        stubIndexQuery(Stream.of(page));
        when(page.items()).thenReturn(List.of());
        when(page.lastEvaluatedKey()).thenReturn(lastKey);
        when(cursorCodec.encode(lastKey)).thenThrow(new IllegalStateException("no gsiPk"));

        assertThatThrownBy(() -> repository.findActivePage(10, null))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findActivePage_noPages_returnsEmptyPage() {
        stubIndexQuery(Stream.empty());

        SmokingReportPage result = repository.findActivePage(10, null);

        assertThat(result.reports()).isEmpty();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void findActivePage_limitOutOfRange_throwsBeforeQuerying() {
        assertThatThrownBy(() -> repository.findActivePage(0, null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> repository.findActivePage(SmokingReportLimits.MAX_LIMIT + 1, null))
            .isInstanceOf(IllegalArgumentException.class);

        verify(smokingReports, never()).index(any());
    }

    @Test
    void findActivePage_maxLimit_isAccepted() {
        stubIndexQuery(Stream.empty());

        repository.findActivePage(SmokingReportLimits.MAX_LIMIT, null);

        assertThat(capturedQueryRequest().limit()).isEqualTo(SmokingReportLimits.MAX_LIMIT);
    }
}
