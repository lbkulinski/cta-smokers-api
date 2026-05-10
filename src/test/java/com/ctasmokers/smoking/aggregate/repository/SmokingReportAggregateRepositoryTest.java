package com.ctasmokers.smoking.aggregate.repository;

import com.ctasmokers.aws.config.DynamoDbTableProperties;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportAggregateRepositoryTest {
    private static final String TABLE_NAME = "smoking-report-aggregates";
    private static final TrainLine LINE = TrainLine.RED;
    private static final String EXPECTED_PK = "LINE#RED";

    @Mock
    private DynamoDbEnhancedClient dynamoDbClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private DynamoDbTable smokingReportAggregates;

    @Mock
    private DynamoDbTableProperties tableProperties;

    private SmokingReportAggregateRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(tableProperties.smokingReportAggregates()).thenReturn(TABLE_NAME);
        when(dynamoDbClient.table(eq(TABLE_NAME), any())).thenReturn(smokingReportAggregates);

        repository = new SmokingReportAggregateRepository(dynamoDbClient, tableProperties);
    }

    @Test
    void findByLineAndDay_found() {
        LocalDate day = LocalDate.of(2026, 5, 10);
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk(EXPECTED_PK)
                                                                 .sk("DAY#2026-05-10")
                                                                 .reportCount(5)
                                                                 .build();

        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(aggregate);

        Optional<SmokingReportAggregate> result = repository.findByLineAndDay(LINE, day);

        assertThat(result).contains(aggregate);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportAggregates).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo("DAY#2026-05-10");
    }

    @Test
    void findByLineAndDay_notFound() {
        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReportAggregate> result = repository.findByLineAndDay(LINE, LocalDate.of(2026, 5, 10));

        assertThat(result).isEmpty();
    }

    @Test
    void findByLineAndWeek_found() {
        YearWeek yearWeek = new YearWeek(2026, 13);
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk(EXPECTED_PK)
                                                                 .sk("WEEK#2026-W13")
                                                                 .reportCount(10)
                                                                 .build();

        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(aggregate);

        Optional<SmokingReportAggregate> result = repository.findByLineAndWeek(LINE, yearWeek);

        assertThat(result).contains(aggregate);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportAggregates).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo("WEEK#2026-W13");
    }

    @Test
    void findByLineAndWeek_notFound() {
        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReportAggregate> result = repository.findByLineAndWeek(LINE, new YearWeek(2026, 13));

        assertThat(result).isEmpty();
    }

    @Test
    void findByLineAndMonth_found() {
        YearMonth yearMonth = YearMonth.of(2026, 3);
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk(EXPECTED_PK)
                                                                 .sk("MONTH#2026-03")
                                                                 .reportCount(42)
                                                                 .build();

        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(aggregate);

        Optional<SmokingReportAggregate> result = repository.findByLineAndMonth(LINE, yearMonth);

        assertThat(result).contains(aggregate);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportAggregates).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo("MONTH#2026-03");
    }

    @Test
    void findByLineAndMonth_notFound() {
        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReportAggregate> result = repository.findByLineAndMonth(LINE, YearMonth.of(2026, 3));

        assertThat(result).isEmpty();
    }

    @Test
    void findByLineAndYear_found() {
        Year year = Year.of(2026);
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk(EXPECTED_PK)
                                                                 .sk("YEAR#2026")
                                                                 .reportCount(100)
                                                                 .build();

        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(aggregate);

        Optional<SmokingReportAggregate> result = repository.findByLineAndYear(LINE, year);

        assertThat(result).contains(aggregate);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportAggregates).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo("YEAR#2026");
    }

    @Test
    void findByLineAndYear_notFound() {
        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReportAggregate> result = repository.findByLineAndYear(LINE, Year.of(2026));

        assertThat(result).isEmpty();
    }

    @Test
    void findByLineAllTime_found() {
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk(EXPECTED_PK)
                                                                 .sk("ALL_TIME")
                                                                 .reportCount(999)
                                                                 .build();

        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(aggregate);

        Optional<SmokingReportAggregate> result = repository.findByLineAllTime(LINE);

        assertThat(result).contains(aggregate);

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(smokingReportAggregates).getItem(keyCaptor.capture());
        assertThat(keyCaptor.getValue().partitionKeyValue().s()).isEqualTo(EXPECTED_PK);
        assertThat(keyCaptor.getValue().sortKeyValue().get().s()).isEqualTo("ALL_TIME");
    }

    @Test
    void findByLineAllTime_notFound() {
        when(smokingReportAggregates.getItem(any(Key.class))).thenReturn(null);

        Optional<SmokingReportAggregate> result = repository.findByLineAllTime(LINE);

        assertThat(result).isEmpty();
    }
}
