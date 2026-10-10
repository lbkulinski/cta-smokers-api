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
    private static final String SK_PREFIX_DAY = "DAY#";

    private final SmokingReportAggregateRepository aggregateRepository;

    @Autowired
    public SmokingReportAggregateService(SmokingReportAggregateRepository aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    public SmokingReportAggregate getDayAggregate(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndDay(line, day);

        if (aggregate == null) {
            throw new SmokingReportAggregateNotFoundException(line, day);
        }

        return aggregate;
    }

    public SmokingReportAggregate getWeekAggregate(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndWeek(line, yearWeek);

        if (aggregate == null) {
            throw new SmokingReportAggregateNotFoundException(line, yearWeek);
        }

        return aggregate;
    }

    public SmokingReportAggregate getMonthAggregate(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndMonth(line, yearMonth);

        if (aggregate == null) {
            throw new SmokingReportAggregateNotFoundException(line, yearMonth);
        }

        return aggregate;
    }

    public SmokingReportAggregate getYearAggregate(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAndYear(line, year);

        if (aggregate == null) {
            throw new SmokingReportAggregateNotFoundException(line, year);
        }

        return aggregate;
    }

    public SmokingReportAggregate getAllTimeAggregate(TrainLine line) {
        Objects.requireNonNull(line);

        SmokingReportAggregate aggregate = this.aggregateRepository.findByLineAllTime(line);

        if (aggregate == null) {
            throw new SmokingReportAggregateNotFoundException(line);
        }

        return aggregate;
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
