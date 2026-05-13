package com.ctasmokers.smoking.aggregate.service;

import com.ctasmokers.smoking.aggregate.dto.SmokingReportAggregateResponse;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCount;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCountsResponse;
import com.ctasmokers.smoking.aggregate.exception.SmokingReportAggregateNotFoundException;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
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
    private final SmokingReportAggregateRepository smokingReportAggregateRepository;

    @Autowired
    public SmokingReportAggregateService(SmokingReportAggregateRepository smokingReportAggregateRepository) {
        this.smokingReportAggregateRepository = smokingReportAggregateRepository;
    }

    public SmokingReportAggregateResponse getDayAggregate(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        return this.smokingReportAggregateRepository.findByLineAndDay(line, day)
                                                    .map(SmokingReportAggregateResponse::from)
                                                    .orElseThrow(() ->
                                                        new SmokingReportAggregateNotFoundException(line, day));
    }

    public SmokingReportAggregateResponse getWeekAggregate(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        return this.smokingReportAggregateRepository.findByLineAndWeek(line, yearWeek)
                                                    .map(SmokingReportAggregateResponse::from)
                                                    .orElseThrow(() ->
                                                        new SmokingReportAggregateNotFoundException(line, yearWeek));
    }

    public SmokingReportAggregateResponse getMonthAggregate(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        return this.smokingReportAggregateRepository.findByLineAndMonth(line, yearMonth)
                                                    .map(SmokingReportAggregateResponse::from)
                                                    .orElseThrow(() ->
                                                        new SmokingReportAggregateNotFoundException(line, yearMonth));
    }

    public SmokingReportAggregateResponse getYearAggregate(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        return this.smokingReportAggregateRepository.findByLineAndYear(line, year)
                                                    .map(SmokingReportAggregateResponse::from)
                                                    .orElseThrow(() ->
                                                        new SmokingReportAggregateNotFoundException(line, year));
    }

    public SmokingReportAggregateResponse getAllTimeAggregate(TrainLine line) {
        Objects.requireNonNull(line);

        return this.smokingReportAggregateRepository.findByLineAllTime(line)
                                                    .map(SmokingReportAggregateResponse::from)
                                                    .orElseThrow(() ->
                                                        new SmokingReportAggregateNotFoundException(line));
    }

    public SmokingReportDailyCountsResponse getDailyCounts(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        List<SmokingReportAggregate> aggregates = this.smokingReportAggregateRepository.findDayAggregatesByLineAndMonth(
            line,
            yearMonth
        );

        List<SmokingReportDailyCount> days = aggregates.stream()
                                                        .map(aggregate -> {
                                                            String dateString = aggregate.sk()
                                                                                         .substring("DAY#".length());

                                                            return new SmokingReportDailyCount(
                                                                LocalDate.parse(dateString),
                                                                aggregate.reportCount()
                                                            );
                                                        })
                                                        .toList();

        return new SmokingReportDailyCountsResponse(days);
    }
}
