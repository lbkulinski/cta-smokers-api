package com.ctasmokers.smoking.report.cursor;

import com.ctasmokers.smoking.report.exception.InvalidCursorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmokingReportCursorCodecTest {
    private SmokingReportCursorCodec codec;

    @BeforeEach
    void setUp() {
        codec = new SmokingReportCursorCodec(JsonMapper.builder().build());
    }

    private static Map<String, AttributeValue> key() {
        return Map.of(
            "carNumberLine", AttributeValue.fromS("2435#RED"),
            "gsiPk", AttributeValue.fromS("REPORT"),
            "expiresAt", AttributeValue.fromN("1771712002"));
    }

    private static String encodeJson(String json) {
        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void encodeThenDecode_roundTripsKey() {
        String cursor = codec.encode(key());

        assertThat(codec.decode(cursor)).isEqualTo(key());
    }

    @Test
    void encode_producesUrlSafeBase64OfCursorJson() {
        String cursor = codec.encode(key());

        assertThat(cursor).matches("[A-Za-z0-9_=-]+");

        var json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        assertThat(json).contains("\"carNumberLine\":\"2435#RED\"")
                        .contains("\"gsiPk\":\"REPORT\"")
                        .contains("\"expiresAt\":1771712002");
    }

    @Test
    void encode_ignoresExtraAttributesInLastEvaluatedKey() {
        var withExtra = new java.util.HashMap<>(key());
        withExtra.put("other", AttributeValue.fromS("ignored"));

        assertThat(codec.decode(codec.encode(withExtra))).isEqualTo(key());
    }

    @Test
    void encode_missingCarNumberLine_throws() {
        var key = new java.util.HashMap<>(key());
        key.remove("carNumberLine");

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("carNumberLine");
    }

    @Test
    void encode_missingGsiPk_throws() {
        var key = new java.util.HashMap<>(key());
        key.remove("gsiPk");

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("gsiPk");
    }

    @Test
    void encode_missingExpiresAt_throws() {
        var key = new java.util.HashMap<>(key());
        key.remove("expiresAt");

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expiresAt");
    }

    @Test
    void encode_stringAttributeIsNotAString_throws() {
        var key = new java.util.HashMap<>(key());
        key.put("carNumberLine", AttributeValue.fromN("1"));

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not a string");
    }

    @Test
    void encode_expiresAtIsNotANumber_throws() {
        var key = new java.util.HashMap<>(key());
        key.put("expiresAt", AttributeValue.fromS("soon"));

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not a number");
    }

    @Test
    void encode_expiresAtIsNotParseable_throws() {
        var key = new java.util.HashMap<>(key());
        key.put("expiresAt", AttributeValue.fromN("1.5"));

        assertThatThrownBy(() -> codec.encode(key))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not parseable");
    }

    @Test
    void decode_notBase64_throwsInvalidCursor() {
        assertThatThrownBy(() -> codec.decode("***not base64***"))
            .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decode_notJson_throwsInvalidCursor() {
        assertThatThrownBy(() -> codec.decode(encodeJson("not json")))
            .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decode_missingFields_throwsInvalidCursor() {
        assertThatThrownBy(() -> codec.decode(encodeJson("{\"carNumberLine\":\"2435#RED\"}")))
            .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decode_negativeExpiresAt_throwsInvalidCursor() {
        String json = "{\"carNumberLine\":\"2435#RED\",\"gsiPk\":\"REPORT\",\"expiresAt\":-1}";

        assertThatThrownBy(() -> codec.decode(encodeJson(json)))
            .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decode_legacyCursor_throwsInvalidCursor() {
        assertThatThrownBy(() -> codec.decode("1771710202399_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f"))
            .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void encode_serializationFails_throwsIllegalState() {
        JsonMapper failingMapper = mock(JsonMapper.class);

        when(failingMapper.writeValueAsString(any())).thenThrow(new JacksonException("boom") {});

        var failingCodec = new SmokingReportCursorCodec(failingMapper);

        assertThatThrownBy(() -> failingCodec.encode(key()))
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(JacksonException.class);
    }
}
