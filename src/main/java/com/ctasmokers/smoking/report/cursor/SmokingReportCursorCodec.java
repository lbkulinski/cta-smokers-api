package com.ctasmokers.smoking.report.cursor;

import com.ctasmokers.smoking.report.exception.InvalidCursorException;
import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
@NullMarked
public final class SmokingReportCursorCodec {
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

    public ReportPageCursor decode(String cursor) {
        Objects.requireNonNull(cursor);

        byte[] cursorBytes;

        try {
            cursorBytes = Base64.getUrlDecoder().decode(cursor);
        } catch (IllegalArgumentException e) {
            throw new InvalidCursorException(cursor);
        }

        var cursorJson = new String(cursorBytes, StandardCharsets.UTF_8);

        ReportPageCursor pageCursor;

        try {
            pageCursor = this.jsonMapper.readValue(cursorJson, ReportPageCursor.class);
        } catch (JacksonException e) {
            throw new InvalidCursorException(cursor);
        }

        //noinspection ConstantValue (Jackson returns null for the JSON literal null)
        if (pageCursor == null || !Objects.equals(pageCursor.gsiPk(), SmokingReportSchema.GSI_PK_VALUE)) {
            throw new InvalidCursorException(cursor);
        }

        return pageCursor;
    }

    private ReportPageCursor toCursor(Map<String, AttributeValue> lastEvaluatedKey) {
        String carNumberLine = this.getRequiredStringAttribute(
            lastEvaluatedKey,
            SmokingReportSchema.CAR_NUMBER_LINE_ATTRIBUTE);

        String gsiPk = this.getRequiredStringAttribute(lastEvaluatedKey, SmokingReportSchema.GSI_PK_ATTRIBUTE);
        long expiresAt = this.getRequiredExpiresAtAttribute(lastEvaluatedKey);

        return new ReportPageCursor(carNumberLine, gsiPk, expiresAt);
    }

    private String getRequiredStringAttribute(Map<String, AttributeValue> key, String attribute) {
        AttributeValue attributeValue = key.get(attribute);

        if (attributeValue == null) {
            throw new IllegalStateException("No %s found".formatted(attribute));
        }

        String value = attributeValue.s();

        if (value == null) {
            throw new IllegalStateException("%s is not a string".formatted(attribute));
        }

        return value;
    }

    private long getRequiredExpiresAtAttribute(Map<String, AttributeValue> key) {
        AttributeValue attributeValue = key.get(SmokingReportSchema.EXPIRES_AT_ATTRIBUTE);

        if (attributeValue == null) {
            throw new IllegalStateException("No %s found".formatted(SmokingReportSchema.EXPIRES_AT_ATTRIBUTE));
        }

        String value = attributeValue.n();

        if (value == null) {
            throw new IllegalStateException("%s is not a number".formatted(SmokingReportSchema.EXPIRES_AT_ATTRIBUTE));
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                "%s is not parseable as a long: %s".formatted(SmokingReportSchema.EXPIRES_AT_ATTRIBUTE, value), e);
        }
    }
}
