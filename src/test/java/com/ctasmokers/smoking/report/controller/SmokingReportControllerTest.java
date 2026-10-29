package com.ctasmokers.smoking.report.controller;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.client.SecretsClient;
import com.ctasmokers.aws.dto.Secret;
import com.ctasmokers.common.config.properties.CorsProperties;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.exception.InvalidCursorException;
import com.ctasmokers.smoking.report.exception.InvalidStationIdException;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.service.SmokingReportService;
import com.rollbar.notifier.Rollbar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmokingReportController.class)
@Import(SmokingReportControllerTest.TestConfig.class)
@EnableConfigurationProperties({CorsProperties.class, CtaReportProperties.class})
class SmokingReportControllerTest {
    private static final String CF_IP = "1.2.3.4";
    private static final String ORIGIN_VERIFY = "test-origin-verify";
    private static final String CURSOR = "c2FtcGxlLWN1cnNvcg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SmokingReportService smokingReportService;

    @MockitoBean
    private Rollbar rollbar;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        SecretsClient awsSecretsClient() {
            SecretsClient client = Mockito.mock(SecretsClient.class);

            Mockito.when(client.getAppSecret())
                   .thenReturn(new Secret(
                       new Secret.CloudflareSecret(ORIGIN_VERIFY),
                       new Secret.RollbarSecret("test-rollbar-access-token")
                   ));

            return client;
        }
    }

    private MockHttpServletRequestBuilder withRequiredHeaders(MockHttpServletRequestBuilder builder) {
        return builder
            .header("CF-Connecting-IP", CF_IP)
            .header("X-Origin-Verify", ORIGIN_VERIFY);
    }

    private SmokingReport sampleReport() {
        return SmokingReport.builder()
                            .carNumberLine("2435#RED")
                            .carNumber("2435")
                            .line(TrainLine.RED)
                            .gsiPk(SmokingReportSchema.GSI_PK_VALUE)
                            .reportedAt(Instant.parse("2026-05-10T12:00:00Z"))
                            .expiresAt(Instant.parse("2026-05-10T12:30:00Z").getEpochSecond())
                            .destinationId("40900")
                            .nextStationId("41220")
                            .runNumber("902")
                            .build();
    }

    private static final String SUBMIT_BODY = """
    {"carNumber":"2435","line":"RED","destinationId":"40900","nextStationId":"41220","runNumber":"902"}
    """;

    @Test
    void submitReport_validRequest_returns201WithLocationAndBody() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any())).thenReturn(sampleReport());

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(SUBMIT_BODY))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location", "https://api.ctasmokers.com/api/cta/reports/smoking/2435/RED"))
               .andExpect(jsonPath("$.carNumber").value("2435"))
               .andExpect(jsonPath("$.line").value("RED"))
               .andExpect(jsonPath("$.reportedAt").value("2026-05-10T12:00:00Z"))
               .andExpect(jsonPath("$.expiresAt").value("2026-05-10T12:30:00Z"))
               .andExpect(jsonPath("$.destinationId").value("40900"))
               .andExpect(jsonPath("$.nextStationId").value("41220"))
               .andExpect(jsonPath("$.runNumber").value("902"))
               .andExpect(jsonPath("$.reportId").doesNotExist())
               .andExpect(jsonPath("$.date").doesNotExist());
    }

    @Test
    void submitReport_passesRequestFieldsToServiceInServiceParameterOrder() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any())).thenReturn(sampleReport());

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(SUBMIT_BODY))
               .andExpect(status().isCreated());

        verify(smokingReportService).submitReport("2435", TrainLine.RED, "40900", "41220", "902");
    }

    @Test
    void submitReport_invalidLineValue_returns400() throws Exception {
        String body = """
        {"carNumber":"2435","line":"INVALID","destinationId":"40900","nextStationId":"41220"}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isBadRequest());
    }

    @Test
    void submitReport_missingLine_returns400() throws Exception {
        String body = """
        {"carNumber":"2435","destinationId":"40900","nextStationId":"41220"}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isBadRequest());
    }

    @Test
    void submitReport_unknownStation_returns400WithProblemDetail() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any()))
            .thenThrow(new InvalidStationIdException("destinationId", "40900"));

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(SUBMIT_BODY))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.title").value("Bad Request"))
               .andExpect(jsonPath("$.detail").value("40900 is not a valid destinationId"))
               .andExpect(jsonPath("$.instance").value("/api/cta/reports/smoking"));
    }

    @Test
    void submitReport_duplicateActiveReport_returns409WithProblemDetail() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any()))
            .thenThrow(new SmokingReportAlreadyExistsException("2435", TrainLine.RED));

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(SUBMIT_BODY))
               .andExpect(status().isConflict())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.title").value("Conflict"))
               .andExpect(jsonPath("$.detail").value("The requested resource already exists."))
               .andExpect(jsonPath("$.instance").value("/api/cta/reports/smoking"));

        verify(rollbar).warning(any(String.class));
    }

    @Test
    void getActiveReports_noParams_returns200AndPassesNulls() throws Exception {
        var page = new SmokingReportPage(List.of(sampleReport()), null);
        when(smokingReportService.getActiveReports(null, null)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reports").isArray())
               .andExpect(jsonPath("$.reports[0].carNumber").value("2435"))
               .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void getActiveReports_withLimitAndCursor_passesThemToService() throws Exception {
        var page = new SmokingReportPage(List.of(), null);
        when(smokingReportService.getActiveReports(10, CURSOR)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active"))
                   .param("limit", "10")
                   .param("nextCursor", CURSOR))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reports").isEmpty());

        verify(smokingReportService).getActiveReports(10, CURSOR);
    }

    @Test
    void getActiveReports_returnsNextCursorFromService() throws Exception {
        var page = new SmokingReportPage(List.of(sampleReport()), CURSOR);
        when(smokingReportService.getActiveReports(null, null)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.nextCursor").value(CURSOR));
    }

    @Test
    void getActiveReports_maxLimit_isAccepted() throws Exception {
        var page = new SmokingReportPage(List.of(), null);
        when(smokingReportService.getActiveReports(100, null)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")).param("limit", "100"))
               .andExpect(status().isOk());
    }

    @Test
    void getActiveReports_limitBelowMinimum_returns400WithoutCallingService() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")).param("limit", "0"))
               .andExpect(status().isBadRequest());

        verify(smokingReportService, never()).getActiveReports(any(), any());
    }

    @Test
    void getActiveReports_limitAboveMaximum_returns400WithoutCallingService() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")).param("limit", "101"))
               .andExpect(status().isBadRequest());

        verify(smokingReportService, never()).getActiveReports(any(), any());
    }

    @Test
    void getActiveReports_nonNumericLimit_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active")).param("limit", "many"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getActiveReports_invalidCursor_returns400WithProblemDetail() throws Exception {
        when(smokingReportService.getActiveReports(eq(null), eq("not-a-valid-cursor")))
            .thenThrow(new InvalidCursorException("not-a-valid-cursor"));

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/active"))
                   .param("nextCursor", "not-a-valid-cursor"))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.detail").value("not-a-valid-cursor is not a valid cursor"));
    }

    @Test
    void getReportByCarNumberLine_found_returns200() throws Exception {
        when(smokingReportService.getReportByCarNumberLine("2435", TrainLine.RED)).thenReturn(sampleReport());

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{carNumber}/{line}", "2435", "RED")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.carNumber").value("2435"))
               .andExpect(jsonPath("$.line").value("RED"));
    }

    @Test
    void getReportByCarNumberLine_notFound_returns404WithProblemDetail() throws Exception {
        when(smokingReportService.getReportByCarNumberLine(any(), any()))
            .thenThrow(new SmokingReportNotFoundException("2435", TrainLine.RED));

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{carNumber}/{line}", "2435", "RED")))
               .andExpect(status().isNotFound())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.title").value("Not Found"))
               .andExpect(jsonPath("$.detail").value("The requested resource was not found."))
               .andExpect(jsonPath("$.instance").value("/api/cta/reports/smoking/2435/RED"));
    }

    @Test
    void getReportByCarNumberLine_invalidLine_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{carNumber}/{line}", "2435", "SILVER")))
               .andExpect(status().isBadRequest());

        verify(smokingReportService, never()).getReportByCarNumberLine(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"243", "24355", "abcd", "24a5"})
    void getReportByCarNumberLine_malformedCarNumber_returns400WithoutCallingService(String carNumber)
        throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{carNumber}/{line}", carNumber, "RED")))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(400));

        verify(smokingReportService, never()).getReportByCarNumberLine(any(), any());
    }

    @Test
    void getReportByCarNumberLine_unexpectedException_returns500WithProblemDetail() throws Exception {
        when(smokingReportService.getReportByCarNumberLine(any(), any()))
            .thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{carNumber}/{line}", "2435", "RED")))
               .andExpect(status().isInternalServerError())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(500))
               .andExpect(jsonPath("$.title").value("Internal Server Error"))
               .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
               .andExpect(jsonPath("$.instance").value("/api/cta/reports/smoking/2435/RED"));
    }

    @Test
    void request_missingOriginVerifyHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/active")
                   .header("CF-Connecting-IP", CF_IP))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_missingCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/active")
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_blankCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/active")
                   .header("CF-Connecting-IP", "   ")
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_invalidIpInCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/active")
                   .header("CF-Connecting-IP", "not-an-ip")
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_wrongOriginVerifyValue_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/active")
                   .header("CF-Connecting-IP", CF_IP)
                   .header("X-Origin-Verify", "wrong-value"))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_healthCheckPath_bypassesFilter() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isNotFound());
    }
}
