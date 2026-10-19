package com.ctasmokers.smoking.common;

import com.cta4j.train.common.model.TrainLine;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public final class StringToTrainLineConverter implements Converter<String, TrainLine> {
    @Override
    public TrainLine convert(String source) {
        try {
            return TrainLine.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TrainLine: %s".formatted(source), e);
        }
    }
}
