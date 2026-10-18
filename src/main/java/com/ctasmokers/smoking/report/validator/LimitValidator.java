package com.ctasmokers.smoking.report.validator;

import com.ctasmokers.smoking.report.config.CtaReportProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public final class LimitValidator implements ConstraintValidator<ValidLimit, Integer> {
    private final int maxLimit;

    @Autowired
    public LimitValidator(CtaReportProperties reportProperties) {
        this.maxLimit = reportProperties.maxLimit();
    }

    @Override
    public boolean isValid(Integer limit, ConstraintValidatorContext context) {
        if (limit == null) {
            return true;
        }

        if (limit > 0 && limit <= this.maxLimit) {
            return true;
        }

        var errorMessage = "limit must be between 1 and %d (inclusive)".formatted(this.maxLimit);

        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(errorMessage)
               .addConstraintViolation();

        return false;
    }
}
