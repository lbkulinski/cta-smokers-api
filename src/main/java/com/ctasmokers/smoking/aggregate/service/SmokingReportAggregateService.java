package com.ctasmokers.smoking.aggregate.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.aggregate.repository.SmokingReportAggregateRepository;
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
public final class SmokingReportAggregateService {
    private static final String SK_PREFIX_DAY = "DAY#";

    private final SmokingReportAggregateRepository aggregateRepository;

    @Autowired
    public SmokingReportAggregateService(SmokingReportAggregateRepository aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    public long getDayAggregate(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndDay(line, day);

        return aggregate == null
            ? 0
            : aggregate.reportCount();
    }

    public long getWeekAggregate(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndWeek(line, yearWeek);

        return aggregate == null
            ? 0
            : aggregate.reportCount();
    }

    public long getMonthAggregate(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndMonth(line, yearMonth);

        return aggregate == null
            ? 0
            : aggregate.reportCount();
    }

    public long getYearAggregate(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndYear(line, year);

        return aggregate == null
            ? 0
            : aggregate.reportCount();
    }

    public long getAllTimeAggregate(TrainLine line) {
        Objects.requireNonNull(line);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAllTime(line);

        return aggregate == null
            ? 0
            : aggregate.reportCount();
    }

    public List<SmokingReportDailyCount> getDailyCounts(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        List<SmokingReportAggregate> aggregates = this.aggregateRepository.findDayAggregatesByLineAndMonth(
            line,
            yearMonth);

        return aggregates.stream()
                         .map(aggregate -> {
                             var dateString = aggregate.sk().substring(SK_PREFIX_DAY.length());
                             var date = LocalDate.parse(dateString);

                             return new SmokingReportDailyCount(date, aggregate.reportCount());
                         })
                         .toList();
    }
}
