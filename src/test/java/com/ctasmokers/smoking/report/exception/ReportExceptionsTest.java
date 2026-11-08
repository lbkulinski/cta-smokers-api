package com.ctasmokers.smoking.report.exception;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExceptionsTest {
    @Test
    void invalidStationId_messageNamesField() {
        var exception = new InvalidStationIdException("destinationId");

        assertThat(exception.getMessage()).isEqualTo("The specified destinationId is invalid.");
    }

    @Test
    void alreadyExists_messageNamesCarAndLine() {
        var exception = new SmokingReportAlreadyExistsException("2435", TrainLine.RED);

        assertThat(exception.getMessage()).contains("2435").contains("RED");
    }

    @Test
    void notFound_messageNamesCarAndLine() {
        var exception = new SmokingReportNotFoundException("2435", TrainLine.BLUE);

        assertThat(exception.getMessage()).contains("2435").contains("BLUE");
    }
}
