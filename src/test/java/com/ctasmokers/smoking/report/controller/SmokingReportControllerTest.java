package com.ctasmokers.smoking.report.controller;

import com.ctasmokers.aws.client.AwsSecretsClient;
import com.ctasmokers.aws.dto.Secret;
import com.ctasmokers.common.config.properties.CorsProperties;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.service.SmokingReportService;
import com.rollbar.notifier.Rollbar;
import org.junit.jupiter.api.Test;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);
    private static final String REPORT_ID = "1771710202399_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f";

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
        AwsSecretsClient awsSecretsClient() {
            AwsSecretsClient client = Mockito.mock(AwsSecretsClient.class);

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
                            .date(DATE)
                            .reportId(REPORT_ID)
                            .reportedAt(Instant.parse("2026-05-10T12:00:00Z"))
                            .expiresAt(Instant.parse("2026-05-10T12:30:00Z").getEpochSecond())
                            .line(TrainLine.RED)
                            .destinationId("40900")
                            .nextStationId("41220")
                            .carNumber("2435")
                            .build();
    }

    @Test
    void submitReport_validRequest_returns201WithLocationAndBody() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any())).thenReturn(sampleReport());

        String body = """
        {"line":"RED","destinationId":"40900","nextStationId":"41220","carNumber":"2435","runNumber":null}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location",
                   "https://api.ctasmokers.com/api/cta/reports/smoking/2026-05-10/" + REPORT_ID))
               .andExpect(jsonPath("$.reportId").value(REPORT_ID))
               .andExpect(jsonPath("$.line").value("RED"));
    }

    @Test
    void submitReport_invalidLineValue_returns400() throws Exception {
        String body = """
        {"line":"INVALID","destinationId":"40900","nextStationId":"41220","carNumber":"2435","runNumber":null}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isBadRequest());
    }

    @Test
    void submitReport_missingLine_returns400() throws Exception {
        String body = """
        {"destinationId":"40900","nextStationId":"41220","carNumber":"2435"}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isBadRequest());
    }

    @Test
    void submitReport_duplicateActiveReport_returns409WithProblemDetail() throws Exception {
        when(smokingReportService.submitReport(any(), any(), any(), any(), any()))
            .thenThrow(new SmokingReportAlreadyExistsException("2435", TrainLine.RED));

        String body = """
        {"line":"RED","destinationId":"40900","nextStationId":"41220","carNumber":"2435","runNumber":null}
        """;

        mockMvc.perform(withRequiredHeaders(post("/api/cta/reports/smoking"))
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(body))
               .andExpect(status().isConflict())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(409))
               .andExpect(jsonPath("$.title").value("Conflict"))
               .andExpect(jsonPath("$.detail").value("The requested resource already exists."))
               .andExpect(jsonPath("$.instance").value("/api/cta/reports/smoking"));
    }

    @Test
    void getReportsByDate_returns200WithReports() throws Exception {
        SmokingReportPage page = new SmokingReportPage(List.of(sampleReport()), null);
        when(smokingReportService.getReportsByDate(DATE, null)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reports").isArray())
               .andExpect(jsonPath("$.reports[0].reportId").value(REPORT_ID));
    }

    @Test
    void getReportsByDate_withValidCursor_passesItToService() throws Exception {
        SmokingReportPage page = new SmokingReportPage(List.of(), null);
        when(smokingReportService.getReportsByDate(DATE, REPORT_ID)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE))
                   .param("nextCursor", REPORT_ID))
               .andExpect(status().isOk());

        verify(smokingReportService).getReportsByDate(DATE, REPORT_ID);
    }

    @Test
    void getReportsByDate_returnsNextCursorFromService() throws Exception {
        SmokingReportPage page = new SmokingReportPage(List.of(sampleReport()), REPORT_ID);
        when(smokingReportService.getReportsByDate(DATE, null)).thenReturn(page);

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.nextCursor").value(REPORT_ID));
    }

    @Test
    void getReportsByDate_withInvalidCursor_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE))
                   .param("nextCursor", "not-a-valid-cursor"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getReportById_found_returns200() throws Exception {
        when(smokingReportService.getReportById(DATE, REPORT_ID)).thenReturn(sampleReport());

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}/{reportId}", DATE, REPORT_ID)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportId").value(REPORT_ID))
               .andExpect(jsonPath("$.line").value("RED"));
    }

    @Test
    void getReportById_notFound_returns404WithProblemDetail() throws Exception {
        when(smokingReportService.getReportById(any(), any()))
            .thenThrow(new SmokingReportNotFoundException(DATE, REPORT_ID));

        String path = "/api/cta/reports/smoking/2026-05-10/" + REPORT_ID;

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}/{reportId}", DATE, REPORT_ID)))
               .andExpect(status().isNotFound())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.title").value("Not Found"))
               .andExpect(jsonPath("$.detail").value("The requested resource was not found."))
               .andExpect(jsonPath("$.instance").value(path));
    }

    @Test
    void getReportById_invalidReportIdFormat_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}/{reportId}", DATE, "not-valid")))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getReportsByDate_cursorWithInvalidTimestamp_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE))
                   .param("nextCursor", "not-a-timestamp_7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getReportsByDate_cursorWithInvalidUuid_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}", DATE))
                   .param("nextCursor", "1771710202399_not-a-uuid"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void request_missingOriginVerifyHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/{date}", DATE)
                   .header("CF-Connecting-IP", CF_IP))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_missingCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/{date}", DATE)
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_blankCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/{date}", DATE)
                   .header("CF-Connecting-IP", "   ")
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_invalidIpInCfConnectingIpHeader_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/{date}", DATE)
                   .header("CF-Connecting-IP", "not-an-ip")
                   .header("X-Origin-Verify", ORIGIN_VERIFY))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_wrongOriginVerifyValue_returns403() throws Exception {
        mockMvc.perform(get("/api/cta/reports/smoking/{date}", DATE)
                   .header("CF-Connecting-IP", CF_IP)
                   .header("X-Origin-Verify", "wrong-value"))
               .andExpect(status().isForbidden());
    }

    @Test
    void request_healthCheckPath_bypassesFilter() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isNotFound());
    }

    @Test
    void getReportById_unexpectedException_returns500WithProblemDetail() throws Exception {
        when(smokingReportService.getReportById(any(), any()))
            .thenThrow(new RuntimeException("unexpected"));

        String path = "/api/cta/reports/smoking/2026-05-10/" + REPORT_ID;

        mockMvc.perform(withRequiredHeaders(get("/api/cta/reports/smoking/{date}/{reportId}", DATE, REPORT_ID)))
               .andExpect(status().isInternalServerError())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(500))
               .andExpect(jsonPath("$.title").value("Internal Server Error"))
               .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
               .andExpect(jsonPath("$.instance").value(path));
    }
}
