package com.ctasmokers.smoking.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public final class ReportIdValidator implements ConstraintValidator<ValidReportId, String> {
    private static final String REPORT_ID_DELIMITER = "_";
    private static final int REPORT_ID_PARTS_COUNT = 2;
    private static final int REPORT_ID_TIMESTAMP_INDEX = 0;
    private static final int REPORT_ID_UUID_INDEX = 1;

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        if (string == null) {
            return true;
        }

        String[] parts = string.split(REPORT_ID_DELIMITER);

        if (parts.length != REPORT_ID_PARTS_COUNT) {
            return false;
        }

        try {
            Long.parseLong(parts[REPORT_ID_TIMESTAMP_INDEX]);
        } catch (NumberFormatException e) {
            return false;
        }

        try {
            UUID.fromString(parts[REPORT_ID_UUID_INDEX]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
