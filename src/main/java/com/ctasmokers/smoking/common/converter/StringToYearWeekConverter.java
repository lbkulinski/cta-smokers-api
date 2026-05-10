package com.ctasmokers.smoking.common.converter;

import com.ctasmokers.smoking.common.model.YearWeek;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public final class StringToYearWeekConverter implements Converter<String, YearWeek> {
    private static final String DELIMITER = "-W";

    @Override
    public YearWeek convert(String source) {
        int delimiterIndex = source.lastIndexOf(DELIMITER);

        if (delimiterIndex == -1) {
            throw new IllegalArgumentException("Invalid YearWeek format: %s".formatted(source));
        }

        int year = Integer.parseInt(source.substring(0, delimiterIndex));
        int week = Integer.parseInt(source.substring(delimiterIndex + DELIMITER.length()));

        return new YearWeek(year, week);
    }
}
