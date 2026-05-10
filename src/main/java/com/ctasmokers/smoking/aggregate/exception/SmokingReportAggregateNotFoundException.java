package com.ctasmokers.smoking.aggregate.exception;

import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public final class SmokingReportAggregateNotFoundException extends RuntimeException {
    public SmokingReportAggregateNotFoundException(TrainLine line, LocalDate day) {
        super("Smoking report aggregate not found for line %s and day %s".formatted(line, day));
    }

    public SmokingReportAggregateNotFoundException(TrainLine line, YearWeek yearWeek) {
        super("Smoking report aggregate not found for line %s and week %s".formatted(line, yearWeek));
    }

    public SmokingReportAggregateNotFoundException(TrainLine line, YearMonth yearMonth) {
        super("Smoking report aggregate not found for line %s and month %s".formatted(line, yearMonth));
    }

    public SmokingReportAggregateNotFoundException(TrainLine line, Year year) {
        super("Smoking report aggregate not found for line %s and year %s".formatted(line, year));
    }

    public SmokingReportAggregateNotFoundException(TrainLine line) {
        super("Smoking report aggregate not found for line %s".formatted(line));
    }
}
