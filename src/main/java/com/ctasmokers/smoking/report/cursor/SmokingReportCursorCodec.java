package com.ctasmokers.smoking.report.cursor;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
@NullMarked
public final class SmokingReportCursorCodec {
    private static final String CAR_NUMBER_LINE_ATTRIBUTE = "carNumberLine";
    private static final String REPORT_DATE_ATTRIBUTE = "reportDate";
    private static final String EXPIRES_AT_ATTRIBUTE = "expiresAt";

    private final JsonMapper jsonMapper;

    @Autowired
    public SmokingReportCursorCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String encode(Map<String, AttributeValue> lastEvaluatedKey) {
        Objects.requireNonNull(lastEvaluatedKey);

        var copy = Map.copyOf(lastEvaluatedKey);

        ReportPageCursor cursor = this.toCursor(copy);

        String cursorJson;

        try {
            cursorJson = this.jsonMapper.writeValueAsString(cursor);
        } catch (JacksonException e) {
            throw new IllegalStateException("Could not serialize cursor to JSON", e);
        }

        byte[] cursorBytes = cursorJson.getBytes(StandardCharsets.UTF_8);

        return Base64.getUrlEncoder().encodeToString(cursorBytes);
    }

    public Map<String, AttributeValue> decode(String cursor) {
        Objects.requireNonNull(cursor);

        byte[] cursorBytes = Base64.getUrlDecoder().decode(cursor);

        var cursorJson = new String(cursorBytes, StandardCharsets.UTF_8);

        ReportPageCursor pageCursor;

        try {
            pageCursor = this.jsonMapper.readValue(cursorJson, ReportPageCursor.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not deserialize cursor from JSON", e);
        }

        return this.toKey(pageCursor);
    }

    private ReportPageCursor toCursor(Map<String, AttributeValue> lastEvaluatedKey) {
        String carNumberLine = this.getRequiredStringAttribute(lastEvaluatedKey, CAR_NUMBER_LINE_ATTRIBUTE);
        String reportDateString = this.getRequiredStringAttribute(lastEvaluatedKey, REPORT_DATE_ATTRIBUTE);
        long expiresAt = this.getRequiredExpiresAtAttribute(lastEvaluatedKey);

        LocalDate reportDate;

        try {
            reportDate = LocalDate.parse(reportDateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "%s is not parseable as a LocalDate: %s".formatted(REPORT_DATE_ATTRIBUTE, reportDateString), e);
        }

        return new ReportPageCursor(carNumberLine, reportDate, expiresAt);
    }

    private Map<String, AttributeValue> toKey(ReportPageCursor cursor) {
        var reportDateString = cursor.reportDate().toString();
        var expiresAtString = Long.toString(cursor.expiresAt());

        return Map.of(
            CAR_NUMBER_LINE_ATTRIBUTE, AttributeValue.createS(cursor.carNumberLine()),
            REPORT_DATE_ATTRIBUTE, AttributeValue.createS(reportDateString),
            EXPIRES_AT_ATTRIBUTE, AttributeValue.createN(expiresAtString)
        );
    }

    private String getRequiredStringAttribute(Map<String, AttributeValue> key, String attribute) {
        AttributeValue attributeValue = key.get(attribute);

        if (attributeValue == null) {
            throw new IllegalArgumentException("No %s found".formatted(attribute));
        }

        String value = attributeValue.s();

        if (value == null) {
            throw new IllegalArgumentException("%s is not a string".formatted(attribute));
        }

        return value;
    }

    private long getRequiredExpiresAtAttribute(Map<String, AttributeValue> key) {
        AttributeValue attributeValue = key.get(EXPIRES_AT_ATTRIBUTE);

        if (attributeValue == null) {
            throw new IllegalArgumentException("No %s found".formatted(EXPIRES_AT_ATTRIBUTE));
        }

        String value = attributeValue.n();

        if (value == null) {
            throw new IllegalArgumentException("%s is not a number".formatted(EXPIRES_AT_ATTRIBUTE));
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "%s is not parseable as a long: %s".formatted(EXPIRES_AT_ATTRIBUTE, value), e);
        }
    }
}
