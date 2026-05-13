package com.ctasmokers.smoking.aggregate.service;

import com.ctasmokers.smoking.aggregate.dto.SmokingReportAggregateResponse;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCountsResponse;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCount;
import com.ctasmokers.smoking.aggregate.exception.SmokingReportAggregateNotFoundException;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.aggregate.repository.SmokingReportAggregateRepository;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

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
        SmokingReportAggregate aggregate = aggregate(5);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(Optional.of(aggregate));

        SmokingReportAggregateResponse response = service.getDayAggregate(LINE, day);

        assertThat(response.reportCount()).isEqualTo(5);
    }

    @Test
    void getDayAggregate_notFound() {
        LocalDate day = LocalDate.of(2026, 5, 10);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDayAggregate(LINE, day))
            .isInstanceOf(SmokingReportAggregateNotFoundException.class);
    }

    @Test
    void getWeekAggregate_found() {
        YearWeek yearWeek = new YearWeek(2026, 13);
        SmokingReportAggregate aggregate = aggregate(10);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(Optional.of(aggregate));

        SmokingReportAggregateResponse response = service.getWeekAggregate(LINE, yearWeek);

        assertThat(response.reportCount()).isEqualTo(10);
    }

    @Test
    void getWeekAggregate_notFound() {
        YearWeek yearWeek = new YearWeek(2026, 13);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWeekAggregate(LINE, yearWeek))
            .isInstanceOf(SmokingReportAggregateNotFoundException.class);
    }

    @Test
    void getMonthAggregate_found() {
        YearMonth yearMonth = YearMonth.of(2026, 3);
        SmokingReportAggregate aggregate = aggregate(42);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(Optional.of(aggregate));

        SmokingReportAggregateResponse response = service.getMonthAggregate(LINE, yearMonth);

        assertThat(response.reportCount()).isEqualTo(42);
    }

    @Test
    void getMonthAggregate_notFound() {
        YearMonth yearMonth = YearMonth.of(2026, 3);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMonthAggregate(LINE, yearMonth))
            .isInstanceOf(SmokingReportAggregateNotFoundException.class);
    }

    @Test
    void getYearAggregate_found() {
        Year year = Year.of(2026);
        SmokingReportAggregate aggregate = aggregate(100);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(Optional.of(aggregate));

        SmokingReportAggregateResponse response = service.getYearAggregate(LINE, year);

        assertThat(response.reportCount()).isEqualTo(100);
    }

    @Test
    void getYearAggregate_notFound() {
        Year year = Year.of(2026);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getYearAggregate(LINE, year))
            .isInstanceOf(SmokingReportAggregateNotFoundException.class);
    }

    @Test
    void getAllTimeAggregate_found() {
        SmokingReportAggregate aggregate = aggregate(999);

        when(repository.findByLineAllTime(LINE)).thenReturn(Optional.of(aggregate));

        SmokingReportAggregateResponse response = service.getAllTimeAggregate(LINE);

        assertThat(response.reportCount()).isEqualTo(999);
    }

    @Test
    void getAllTimeAggregate_notFound() {
        when(repository.findByLineAllTime(LINE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllTimeAggregate(LINE))
            .isInstanceOf(SmokingReportAggregateNotFoundException.class);
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

        SmokingReportDailyCountsResponse response = service.getDailyCounts(LINE, yearMonth);

        assertThat(response.days()).hasSize(1);
        assertThat(response.days().getFirst().date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(response.days().getFirst().reportCount()).isEqualTo(7);
    }

    @Test
    void smokingReportAggregateResponse_negativeReportCount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new SmokingReportAggregateResponse(-1L))
            .isInstanceOf(IllegalArgumentException.class);
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

        SmokingReportDailyCountsResponse response = service.getDailyCounts(LINE, yearMonth);

        assertThat(response.days()).isEmpty();
    }
}
