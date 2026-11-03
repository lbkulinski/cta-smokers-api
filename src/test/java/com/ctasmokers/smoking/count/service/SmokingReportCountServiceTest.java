package com.ctasmokers.smoking.count.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.count.model.SmokingReportCount;
import com.ctasmokers.smoking.count.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.count.repository.SmokingReportCountRepository;
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
class SmokingReportCountServiceTest {
    private static final TrainLine LINE = TrainLine.RED;

    @Mock
    private SmokingReportCountRepository repository;

    private SmokingReportCountService service;

    @BeforeEach
    void setUp() {
        service = new SmokingReportCountService(repository);
    }

    private SmokingReportCount count(long reportCount) {
        return SmokingReportCount.builder()
                                 .pk("LINE#RED")
                                 .sk("SK")
                                 .reportCount(reportCount)
                                 .build();
    }

    @Test
    void getDayCount_found() {
        LocalDate day = LocalDate.of(2026, 5, 10);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(count(5));

        long reportCount = service.getDayCount(LINE, day);

        assertThat(reportCount).isEqualTo(5);
    }

    @Test
    void getDayCount_notFound_returnsZero() {
        LocalDate day = LocalDate.of(2026, 5, 10);

        when(repository.findByLineAndDay(LINE, day)).thenReturn(null);

        long reportCount = service.getDayCount(LINE, day);

        assertThat(reportCount).isZero();
    }

    @Test
    void getWeekCount_found() {
        YearWeek yearWeek = YearWeek.of(2026, 13);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(count(10));

        long reportCount = service.getWeekCount(LINE, yearWeek);

        assertThat(reportCount).isEqualTo(10);
    }

    @Test
    void getWeekCount_notFound_returnsZero() {
        YearWeek yearWeek = YearWeek.of(2026, 13);

        when(repository.findByLineAndWeek(LINE, yearWeek)).thenReturn(null);

        long reportCount = service.getWeekCount(LINE, yearWeek);

        assertThat(reportCount).isZero();
    }

    @Test
    void getMonthCount_found() {
        YearMonth yearMonth = YearMonth.of(2026, 3);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(count(42));

        long reportCount = service.getMonthCount(LINE, yearMonth);

        assertThat(reportCount).isEqualTo(42);
    }

    @Test
    void getMonthCount_notFound_returnsZero() {
        YearMonth yearMonth = YearMonth.of(2026, 3);

        when(repository.findByLineAndMonth(LINE, yearMonth)).thenReturn(null);

        long reportCount = service.getMonthCount(LINE, yearMonth);

        assertThat(reportCount).isZero();
    }

    @Test
    void getYearCount_found() {
        Year year = Year.of(2026);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(count(100));

        long reportCount = service.getYearCount(LINE, year);

        assertThat(reportCount).isEqualTo(100);
    }

    @Test
    void getYearCount_notFound_returnsZero() {
        Year year = Year.of(2026);

        when(repository.findByLineAndYear(LINE, year)).thenReturn(null);

        long reportCount = service.getYearCount(LINE, year);

        assertThat(reportCount).isZero();
    }

    @Test
    void getAllTimeCount_found() {
        when(repository.findByLineAllTime(LINE)).thenReturn(count(999));

        long reportCount = service.getAllTimeCount(LINE);

        assertThat(reportCount).isEqualTo(999);
    }

    @Test
    void getAllTimeCount_notFound_returnsZero() {
        when(repository.findByLineAllTime(LINE)).thenReturn(null);

        long reportCount = service.getAllTimeCount(LINE);

        assertThat(reportCount).isZero();
    }

    @Test
    void getDailyCounts_returnsCounts() {
        YearMonth yearMonth = YearMonth.of(2026, 5);
        SmokingReportCount count = SmokingReportCount.builder()
                                                     .pk("LINE#RED")
                                                     .sk("DAY#2026-05-10")
                                                     .reportCount(7)
                                                     .build();

        when(repository.findDayCountsByLineAndMonth(LINE, yearMonth)).thenReturn(List.of(count));

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

        when(repository.findDayCountsByLineAndMonth(LINE, yearMonth)).thenReturn(List.of());

        List<SmokingReportDailyCount> response = service.getDailyCounts(LINE, yearMonth);

        assertThat(response).isEmpty();
    }
}