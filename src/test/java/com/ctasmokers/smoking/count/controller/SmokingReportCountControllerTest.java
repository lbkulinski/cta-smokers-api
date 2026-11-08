package com.ctasmokers.smoking.count.controller;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.client.SecretsClient;
import com.ctasmokers.aws.dto.Secret;
import com.ctasmokers.common.config.properties.CorsProperties;
import com.ctasmokers.smoking.count.converter.StringToYearWeekConverter;
import com.ctasmokers.smoking.count.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.count.service.SmokingReportCountService;
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
import org.springframework.format.FormatterRegistry;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmokingReportCountController.class)
@Import(SmokingReportCountControllerTest.TestConfig.class)
@EnableConfigurationProperties(CorsProperties.class)
class SmokingReportCountControllerTest {
    private static final String CF_IP = "1.2.3.4";
    private static final String ORIGIN_VERIFY = "test-origin-verify";
    private static final String BASE_PATH = "/api/cta/reports/smoking/counts";
    private static final TrainLine LINE = TrainLine.RED;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);
    private static final long REPORT_COUNT = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SmokingReportCountService smokingReportCountService;

    @MockitoBean
    private Rollbar rollbar;

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
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

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverter(new StringToYearWeekConverter());
        }
    }

    private MockHttpServletRequestBuilder withRequiredHeaders(MockHttpServletRequestBuilder builder) {
        return builder
            .header("CF-Connecting-IP", CF_IP)
            .header("X-Origin-Verify", ORIGIN_VERIFY);
    }

    @Test
    void getDayCount_found_returns200() throws Exception {
        when(smokingReportCountService.getDayCount(LINE, DATE)).thenReturn(REPORT_COUNT);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/day/{date}", LINE, DATE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(42));
    }

    @Test
    void getDayCount_noReports_returns200WithZero() throws Exception {
        when(smokingReportCountService.getDayCount(LINE, DATE)).thenReturn(0L);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/day/{date}", LINE, DATE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(0));
    }

    @Test
    void getWeekCount_validFormat_returns200() throws Exception {
        YearWeek yearWeek = YearWeek.of(2026, 13);
        when(smokingReportCountService.getWeekCount(LINE, yearWeek)).thenReturn(REPORT_COUNT);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/week/2026-W13", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(42));
    }

    @Test
    void getWeekCount_invalidFormat_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/week/not-a-week", LINE)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void getMonthCount_validFormat_returns200() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026, 3);
        when(smokingReportCountService.getMonthCount(LINE, yearMonth)).thenReturn(REPORT_COUNT);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/month/2026-03", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(42));
    }

    @Test
    void getYearCount_validFormat_returns200() throws Exception {
        Year year = Year.of(2026);
        when(smokingReportCountService.getYearCount(LINE, year)).thenReturn(REPORT_COUNT);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/year/2026", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(42));
    }

    @Test
    void getAllTimeCount_validLine_returns200() throws Exception {
        when(smokingReportCountService.getAllTimeCount(LINE)).thenReturn(REPORT_COUNT);

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/all-time", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.reportCount").value(42));
    }

    @Test
    void getAllTimeCount_invalidLine_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/INVALID/all-time")))
               .andExpect(status().isBadRequest());
    }

    @Test
    void request_missingOriginVerifyHeader_returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{line}/all-time", LINE)
                   .header("CF-Connecting-IP", CF_IP))
               .andExpect(status().isForbidden());
    }

    @Test
    void getDailyCounts_returns200() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026, 5);
        SmokingReportDailyCount count = new SmokingReportDailyCount(LocalDate.of(2026, 5, 10), 7L);

        when(smokingReportCountService.getDailyCounts(LINE, yearMonth)).thenReturn(List.of(count));

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/month/2026-05/days", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.days[0].date").value("2026-05-10"))
               .andExpect(jsonPath("$.days[0].reportCount").value(7));
    }

    @Test
    void getDailyCounts_emptyMonth_returns200() throws Exception {
        when(smokingReportCountService.getDailyCounts(any(), any()))
            .thenReturn(List.of());

        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/{line}/month/2026-05/days", LINE)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.days").isEmpty());
    }

    @Test
    void getDailyCounts_invalidLine_returns400() throws Exception {
        mockMvc.perform(withRequiredHeaders(get(BASE_PATH + "/INVALID/month/2026-05/days")))
               .andExpect(status().isBadRequest());
    }
}
