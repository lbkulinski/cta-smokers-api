package com.ctasmokers.smoking.count.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.count.model.SmokingReportCount;
import com.ctasmokers.smoking.count.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.count.repository.SmokingReportCountRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
@NullMarked
public final class SmokingReportCountService {
    private static final String SK_PREFIX_DAY = "DAY#";

    private final SmokingReportCountRepository countRepository;

    @Autowired
    public SmokingReportCountService(SmokingReportCountRepository countRepository) {
        this.countRepository = countRepository;
    }

    public long getDayCount(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        SmokingReportCount count = this.countRepository.findByLineAndDay(line, day);

        return count == null
            ? 0
            : count.reportCount();
    }

    public long getWeekCount(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        SmokingReportCount count = this.countRepository.findByLineAndWeek(line, yearWeek);

        return count == null
            ? 0
            : count.reportCount();
    }

    public long getMonthCount(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        SmokingReportCount count = this.countRepository.findByLineAndMonth(line, yearMonth);

        return count == null
            ? 0
            : count.reportCount();
    }

    public long getYearCount(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        SmokingReportCount count = this.countRepository.findByLineAndYear(line, year);

        return count == null
            ? 0
            : count.reportCount();
    }

    public long getAllTimeCount(TrainLine line) {
        Objects.requireNonNull(line);

        SmokingReportCount count = this.countRepository.findByLineAllTime(line);

        return count == null
            ? 0
            : count.reportCount();
    }

    public List<SmokingReportDailyCount> getDailyCounts(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        List<SmokingReportCount> counts = this.countRepository.findDayCountsByLineAndMonth(
            line,
            yearMonth);

        return counts.stream()
                     .map(count -> {
                         var dateString = count.sk().substring(SK_PREFIX_DAY.length());
                         var date = LocalDate.parse(dateString);

                         return new SmokingReportDailyCount(date, count.reportCount());
                     })
                     .toList();
    }
}
