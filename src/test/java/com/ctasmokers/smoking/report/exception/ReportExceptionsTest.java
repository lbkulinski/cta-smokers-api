package com.ctasmokers.smoking.report.exception;

import com.cta4j.train.common.model.TrainLine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExceptionsTest {
    @Test
    void invalidStationId_messageNamesFieldAndValue() {
        var exception = new InvalidStationIdException("destinationId", "99999");

        assertThat(exception.getMessage()).isEqualTo("99999 is not a valid destinationId");
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
