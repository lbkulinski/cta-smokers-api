package com.ctasmokers.smoking.count.converter;

import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.threeten.extra.YearWeek;

import java.time.format.DateTimeParseException;

@Component
@NullMarked
public final class StringToYearWeekConverter implements Converter<String, YearWeek> {
    @Override
    public YearWeek convert(String source) {
        YearWeek yearWeek;

        try {
            yearWeek = YearWeek.parse(source);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid YearWeek format: %s".formatted(source), e);
        }

        return yearWeek;
    }
}
