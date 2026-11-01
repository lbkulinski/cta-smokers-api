package com.ctasmokers.smoking.aggregate.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.aggregate.repository.SmokingReportAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportAggregateServiceTest {
    private static final TrainLine LINE = TrainLine.RED;

    @Mock
    private SmokingReportAggregateRepository repository;

    private SmokingReportAggregateService service;

    @BeforeEach
    void setUp() {
        service = new SmokingReportAggregateService(repository);
    }

    private SmokingReportAggregate aggregate(long reportCount) {
        return SmokingReportAggregate.builder()
                                    .pk("LINE#RED")
                                    .sk("SK")
                                    .reportCount(reportCount)
                                    .build();
    }

    @Test
    void getDayAggregate_found() {
        LocalDate day = LocalDate.of(2026, 5, 10);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(aggregate(5));

        long reportCount = service.getDayAggregate(LINE, day);

        assertThat(reportCount).isEqualTo(5);
    }

    @Test
    void getDayAggregate_notFound_returnsZero() {
        LocalDate day = LocalDate.of(2026, 5, 10);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(null);

        long reportCount = service.getDayAggregate(LINE, day);

        assertThat(reportCount).isZero();
    }

    @Test
    void getWeekAggregate_found() {
        YearWeek yearWeek = YearWeek.of(2026, 13);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(aggregate(10));

        long reportCount = service.getWeekAggregate(LINE, yearWeek);

        assertThat(reportCount).isEqualTo(10);
    }

    @Test
    void getWeekAggregate_notFound_returnsZero() {
        YearWeek yearWeek = YearWeek.of(2026, 13);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(null);

        long reportCount = service.getWeekAggregate(LINE, yearWeek);

        assertThat(reportCount).isZero();
    }

    @Test
    void getMonthAggregate_found() {
        YearMonth yearMonth = YearMonth.of(2026, 3);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(aggregate(42));

        long reportCount = service.getMonthAggregate(LINE, yearMonth);

        assertThat(reportCount).isEqualTo(42);
    }

    @Test
    void getMonthAggregate_notFound_returnsZero() {
        YearMonth yearMonth = YearMonth.of(2026, 3);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(null);

        long reportCount = service.getMonthAggregate(LINE, yearMonth);

        assertThat(reportCount).isZero();
    }

    @Test
    void getYearAggregate_found() {
        Year year = Year.of(2026);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(aggregate(100));

        long reportCount = service.getYearAggregate(LINE, year);

        assertThat(reportCount).isEqualTo(100);
    }

    @Test
    void getYearAggregate_notFound_returnsZero() {
        Year year = Year.of(2026);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(null);

        long reportCount = service.getYearAggregate(LINE, year);

        assertThat(reportCount).isZero();
    }

    @Test
    void getAllTimeAggregate_found() {
        when(repository.findByLineAllTime(LINE)).thenReturn(aggregate(999));

        long reportCount = service.getAllTimeAggregate(LINE);

        assertThat(reportCount).isEqualTo(999);
    }

    @Test
    void getAllTimeAggregate_notFound_returnsZero() {
        when(repository.findByLineAllTime(LINE)).thenReturn(null);

        long reportCount = service.getAllTimeAggregate(LINE);

        assertThat(reportCount).isZero();
    }

    @Test
    void getDailyCounts_returnsCounts() {
        YearMonth yearMonth = YearMonth.of(2026, 5);
        SmokingReportAggregate aggregate = SmokingReportAggregate.builder()
                                                                 .pk("LINE#RED")
                                                                 .sk("DAY#2026-05-10")
                                                                 .reportCount(7)
                                                                 .build();

        when(repository.findDayAggregatesByLineAndMonth(LINE, yearMonth)).thenReturn(List.of(aggregate));

        List<SmokingReportDailyCount> response = service.getDailyCounts(LINE, yearMonth);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(response.getFirst().reportCount()).isEqualTo(7);
    }

    @Test
    void smokingReportDailyCount_negativeReportCount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new SmokingReportDailyCount(LocalDate.of(2026, 5, 10), -1L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getDailyCounts_empty() {
        YearMonth yearMonth = YearMonth.of(2026, 5);

        when(repository.findDayAggregatesByLineAndMonth(LINE, yearMonth)).thenReturn(List.of());

        List<SmokingReportDailyCount> response = service.getDailyCounts(LINE, yearMonth);

        assertThat(response).isEmpty();
    }
}