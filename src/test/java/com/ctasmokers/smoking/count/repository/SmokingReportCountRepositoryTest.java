package com.ctasmokers.smoking.count.repository;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.count.model.SmokingReportCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.threeten.extra.YearWeek;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportCountRepositoryTest {
    private static final String TABLE_NAME = "smoking-report-counts";
    private static final TrainLine LINE = TrainLine.RED;
    private static final String EXPECTED_PK = "LINE#RED";

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbTable smokingReportCounts;

    @Mock
    @SuppressWarnings("rawtypes")
    private PageIterable pageIterable;

    @Mock
    @SuppressWarnings("rawtypes")
    private SdkIterable items;

    @Mock
    private DynamoDbProperties tableProperties;

    private SmokingReportCountRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(tableProperties.smokingReportCounts()).thenReturn(TABLE_NAME);
        when(dynamoDbClient.table(eq(TABLE_NAME), any())).thenReturn(smokingReportCounts);

        repository = new SmokingReportCountRepository(dynamoDbClient, tableProperties);
    }

    @Test
    void findByLineAndDay_found() {
        LocalDate day = LocalDate.of(2026, 5, 10);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("DAY#2026-05-10")
                                                     .reportCount(5)
                                                     .build();

        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(count);

        SmokingReportCount result = repository.findByLineAndDay(LINE, day);

        assertThat(result).isEqualTo(count);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportCounts).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo("DAY#2026-05-10");
    }

    @Test
    void findByLineAndDay_notFound() {
        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(null);

        SmokingReportCount result = repository.findByLineAndDay(LINE, LocalDate.of(2026, 5, 10));

        assertThat(result).isNull();
    }

    @Test
    void findByLineAndWeek_found() {
        YearWeek yearWeek = YearWeek.of(2026, 13);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("WEEK#2026-W13")
                                                     .reportCount(10)
                                                     .build();

        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(count);

        SmokingReportCount result = repository.findByLineAndWeek(LINE, yearWeek);

        assertThat(result).isEqualTo(count);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportCounts).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo("WEEK#2026-W13");
    }

    @Test
    void findByLineAndWeek_notFound() {
        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(null);

        SmokingReportCount result = repository.findByLineAndWeek(LINE, YearWeek.of(2026, 13));

        assertThat(result).isNull();
    }

    @Test
    void findByLineAndMonth_found() {
        YearMonth yearMonth = YearMonth.of(2026, 3);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("MONTH#2026-03")
                                                     .reportCount(42)
                                                     .build();

        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(count);

        SmokingReportCount result = repository.findByLineAndMonth(LINE, yearMonth);

        assertThat(result).isEqualTo(count);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportCounts).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo("MONTH#2026-03");
    }

    @Test
    void findByLineAndMonth_notFound() {
        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(null);

        SmokingReportCount result = repository.findByLineAndMonth(LINE, YearMonth.of(2026, 3));

        assertThat(result).isNull();
    }

    @Test
    void findByLineAndYear_found() {
        Year year = Year.of(2026);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("YEAR#2026")
                                                     .reportCount(100)
                                                     .build();

        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(count);

        SmokingReportCount result = repository.findByLineAndYear(LINE, year);

        assertThat(result).isEqualTo(count);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportCounts).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo("YEAR#2026");
    }

    @Test
    void findByLineAndYear_notFound() {
        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(null);

        SmokingReportCount result = repository.findByLineAndYear(LINE, Year.of(2026));

        assertThat(result).isNull();
    }

    @Test
    void findByLineAllTime_found() {
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("ALL_TIME")
                                                     .reportCount(999)
                                                     .build();

        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(count);

        SmokingReportCount result = repository.findByLineAllTime(LINE);

        assertThat(result).isEqualTo(count);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportCounts).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().orElseThrow().s()).isEqualTo("ALL_TIME");
    }

    @Test
    void findByLineAllTime_notFound() {
        when(smokingReportCounts.getItem(any(Key.class))).thenReturn(null);

        SmokingReportCount result = repository.findByLineAllTime(LINE);

        assertThat(result).isNull();
    }

    @Test
    void findDayCountsByLineAndMonth_returnsItems() {
        YearMonth yearMonth = YearMonth.of(2026, 5);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("DAY#2026-05-10")
                                                     .reportCount(7)
                                                     .build();

        when(smokingReportCounts.query(any(QueryConditional.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(items);
        when(items.stream()).thenReturn(Stream.of(count));

        List<SmokingReportCount> result = repository.findDayCountsByLineAndMonth(LINE, yearMonth);

        assertThat(result).containsExactly(count);

        ArgumentCaptor<QueryConditional> queryCaptor = ArgumentCaptor.forClass(QueryConditional.class);
        verify(smokingReportCounts).query(queryCaptor.capture());

        Key expectedKey = Key.builder()
                             .partitionValue(EXPECTED_PK)
                             .sortValue("DAY#2026-05")
                             .build();
        assertThat(queryCaptor.getValue()).isEqualTo(QueryConditional.sortBeginsWith(expectedKey));
    }

    @Test
    void findDayCountsByLineAndMonth_empty() {
        YearMonth yearMonth = YearMonth.of(2026, 5);

        when(smokingReportCounts.query(any(QueryConditional.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(items);
        when(items.stream()).thenReturn(Stream.of());

        List<SmokingReportCount> result = repository.findDayCountsByLineAndMonth(LINE, yearMonth);

        assertThat(result).isEmpty();
    }

    @Test
    void findDayCountsByLineAndMonth_filtersNullItems() {
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk(EXPECTED_PK)
                                                     .sk("DAY#2026-05-10")
                                                     .reportCount(7)
                                                     .build();

        when(smokingReportCounts.query(any(QueryConditional.class))).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(items);
        when(items.stream()).thenReturn(Stream.of(count, null));

        List<SmokingReportCount> result = repository.findDayCountsByLineAndMonth(LINE, YearMonth.of(2026, 5));

        assertThat(result).containsExactly(count);
    }
}
