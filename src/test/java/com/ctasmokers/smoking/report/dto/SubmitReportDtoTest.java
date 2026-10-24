package com.ctasmokers.smoking.report.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitReportDtoTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private static SubmitReportDto dto(String carNumber, String line, String destinationId, String nextStationId,
                                       String runNumber) {
        return new SubmitReportDto(carNumber, line, destinationId, nextStationId, runNumber);
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(dto("2435", "RED", "40900", "41220", "902"))).isEmpty();
    }

    @Test
    void validRequest_withoutRunNumber_hasNoViolations() {
        assertThat(validator.validate(dto("2435", "RED", "40900", "41220", null))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"RED", "BLUE", "GREEN", "BROWN", "PURPLE", "PINK", "ORANGE", "YELLOW"})
    void line_allCtaLines_valid(String line) {
        assertThat(validator.validate(dto("2435", line, "40900", "41220", null))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"red", "SILVER", "", "RED "})
    void line_invalid_hasViolation(String line) {
        assertThat(validator.validate(dto("2435", line, "40900", "41220", null))).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"243", "24355", "abcd", ""})
    void carNumber_invalid_hasViolation(String carNumber) {
        assertThat(validator.validate(dto(carNumber, "RED", "40900", "41220", null))).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "1234", "abc", ""})
    void runNumber_invalid_hasViolation(String runNumber) {
        assertThat(validator.validate(dto("2435", "RED", "40900", "41220", runNumber))).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "12345678901"})
    void stationIds_invalid_haveViolations(String id) {
        assertThat(validator.validate(dto("2435", "RED", id, id, null))).hasSize(2);
    }

    @Test
    void nullRequiredFields_haveViolations() {
        assertThat(validator.validate(dto(null, null, null, null, null))).hasSize(4);
    }
}
