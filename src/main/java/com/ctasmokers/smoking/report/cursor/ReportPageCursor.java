package com.ctasmokers.smoking.report.cursor;

import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import org.jspecify.annotations.NullMarked;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Objects;

@NullMarked
public record ReportPageCursor(
    String carNumberLine,
    String gsiPk,
    long expiresAt
) {
    public ReportPageCursor {
        Objects.requireNonNull(carNumberLine);
        Objects.requireNonNull(gsiPk);

        if (!SmokingReportSchema.isValidCarNumberLine(carNumberLine)) {
            throw new IllegalArgumentException("%s is not a valid carNumberLine".formatted(carNumberLine));
        }

        if (expiresAt < 0L) {
            throw new IllegalArgumentException("expiresAt must be non-negative");
        }
    }

    public Map<String, AttributeValue> toKey() {
        var expiresAtString = Long.toString(this.expiresAt);

        return Map.of(
            SmokingReportSchema.CAR_NUMBER_LINE_ATTRIBUTE, AttributeValue.fromS(this.carNumberLine),
            SmokingReportSchema.GSI_PK_ATTRIBUTE, AttributeValue.fromS(this.gsiPk),
            SmokingReportSchema.EXPIRES_AT_ATTRIBUTE, AttributeValue.fromN(expiresAtString));
    }
}
