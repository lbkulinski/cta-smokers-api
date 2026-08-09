package com.ctasmokers.smoking.aggregate.service;

import com.ctasmokers.smoking.aggregate.exception.SmokingReportAggregateNotFoundException;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.aggregate.repository.SmokingReportAggregateRepository;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
@NullMarked
public final class SmokingReportAggregateService {
    private final SmokingReportAggregateRepository aggregateRepository;

    @Autowired
    public SmokingReportAggregateService(SmokingReportAggregateRepository aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    public SmokingReportAggregate getDayAggregate(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        return this.aggregateRepository.findByLineAndDay(line, day)
                                       .orElseThrow(() -> new SmokingReportAggregateNotFoundException(line, day));
    }

    public SmokingReportAggregate getWeekAggregate(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        return this.aggregateRepository.findByLineAndWeek(line, yearWeek)
                                       .orElseThrow(() -> new SmokingReportAggregateNotFoundException(line, yearWeek));
    }

    public SmokingReportAggregate getMonthAggregate(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        return this.aggregateRepository.findByLineAndMonth(line, yearMonth)
                                       .orElseThrow(() -> new SmokingReportAggregateNotFoundException(line, yearMonth));
    }

    public SmokingReportAggregate getYearAggregate(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        return this.aggregateRepository.findByLineAndYear(line, year)
                                       .orElseThrow(() -> new SmokingReportAggregateNotFoundException(line, year));
    }

    public SmokingReportAggregate getAllTimeAggregate(TrainLine line) {
        Objects.requireNonNull(line);

        return this.aggregateRepository.findByLineAllTime(line)
                                       .orElseThrow(() -> new SmokingReportAggregateNotFoundException(line));
    }

    public List<SmokingReportDailyCount> getDailyCounts(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        List<SmokingReportAggregate> aggregates = this.aggregateRepository.findDayAggregatesByLineAndMonth(
            line,
            yearMonth
        );

        return aggregates.stream()
                         .map(aggregate -> {
                             String dateString = aggregate.sk().substring("DAY#".length());

                             return new SmokingReportDailyCount(
                                 LocalDate.parse(dateString),
                                 aggregate.reportCount()
                             );
                         })
                         .toList();
    }
}
