package com.ctasmokers.smoking.common.model;

import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.IsoFields;

@NullMarked
public record YearWeek(int year, int week) {
    public YearWeek {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid year %d".formatted(year));
        }

        LocalDate midYear = LocalDate.of(year, Month.JUNE, 1);

        int maxWeek = (int) IsoFields.WEEK_OF_WEEK_BASED_YEAR.rangeRefinedBy(midYear)
                                                             .getMaximum();

        if (week < 1 || week > maxWeek) {
            throw new IllegalArgumentException("Invalid week %d for year %d".formatted(week, year));
        }
    }

    @Override
    public String toString() {
        return "%d-W%02d".formatted(this.year, this.week);
    }
}
