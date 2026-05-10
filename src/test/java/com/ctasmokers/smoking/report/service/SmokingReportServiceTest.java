package com.ctasmokers.smoking.report.service;

import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.dto.SmokingReportResponse;
import com.ctasmokers.smoking.report.dto.SmokingReportsResponse;
import com.ctasmokers.smoking.report.dto.SubmitReportRequest;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository.SmokingReportPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportServiceTest {
    private static final int PAGE_SIZE = 10;
    private static final long EXPIRE_AFTER_MINUTES = 30;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);
    private static final String REPORT_ID = "1234567890_abc-def";

    @Mock
    private SmokingReportRepository repository;

    @Mock
    private CtaReportProperties reportProperties;

    private SmokingReportService service;

    @BeforeEach
    void setUp() {
        when(reportProperties.pageSize()).thenReturn(PAGE_SIZE);
        when(reportProperties.expireAfterMinutes()).thenReturn(EXPIRE_AFTER_MINUTES);

        service = new SmokingReportService(repository, reportProperties);
    }

    private SmokingReport report() {
        return SmokingReport.builder()
                            .date(DATE)
                            .reportId(REPORT_ID)
                            .reportedAt(Instant.now())
                            .expiresAt(Instant.now().getEpochSecond() + 1800)
                            .line(TrainLine.RED)
                            .destinationId("40900")
                            .nextStationId("41220")
                            .carNumber("2435")
                            .build();
    }

    @Test
    void submitReport_savesReportAndReturnsResponse() {
        SubmitReportRequest request = new SubmitReportRequest("RED", "40900", "41220", "2435", null);

        SmokingReportResponse response = service.submitReport(request);

        ArgumentCaptor<SmokingReport> reportCaptor = ArgumentCaptor.forClass(SmokingReport.class);
        verify(repository).save(reportCaptor.capture());

        SmokingReport saved = reportCaptor.getValue();
        assertThat(saved.line()).isEqualTo(TrainLine.RED);
        assertThat(saved.destinationId()).isEqualTo("40900");
        assertThat(saved.nextStationId()).isEqualTo("41220");
        assertThat(saved.carNumber()).isEqualTo("2435");
        assertThat(saved.runNumber()).isNull();

        assertThat(response.line()).isEqualTo(TrainLine.RED);
        assertThat(response.destinationId()).isEqualTo("40900");
    }

    @Test
    void submitReport_withRunNumber_includesRunNumber() {
        SubmitReportRequest request = new SubmitReportRequest("BLUE", "40900", "41220", "2435", "902");

        service.submitReport(request);

        ArgumentCaptor<SmokingReport> reportCaptor = ArgumentCaptor.forClass(SmokingReport.class);
        verify(repository).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().runNumber()).isEqualTo("902");
    }

    @Test
    void getReportsByDate_returnsResponse() {
        SmokingReport report = report();
        SmokingReportPage page = new SmokingReportPage(List.of(report), null);

        when(repository.findPageByDate(DATE, PAGE_SIZE, null)).thenReturn(page);

        SmokingReportsResponse response = service.getReportsByDate(DATE, null);

        assertThat(response.reports()).hasSize(1);
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getReportsByDate_withNextPage_returnsCursor() {
        SmokingReport report = report();
        Map<String, AttributeValue> lastKey = Map.of(
            SmokingReportRepository.REPORT_ID_KEY, AttributeValue.builder().s(REPORT_ID).build()
        );
        SmokingReportPage page = new SmokingReportPage(List.of(report), lastKey);

        when(repository.findPageByDate(DATE, PAGE_SIZE, null)).thenReturn(page);

        SmokingReportsResponse response = service.getReportsByDate(DATE, null);

        assertThat(response.nextCursor()).isEqualTo(REPORT_ID);
    }

    @Test
    void getReportById_found() {
        SmokingReport report = report();

        when(repository.findById(DATE, REPORT_ID)).thenReturn(Optional.of(report));

        SmokingReportResponse response = service.getReportById(DATE, REPORT_ID);

        assertThat(response.reportId()).isEqualTo(REPORT_ID);
        assertThat(response.line()).isEqualTo(TrainLine.RED);
    }

    @Test
    void getReportById_notFound() {
        when(repository.findById(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReportById(DATE, REPORT_ID))
            .isInstanceOf(SmokingReportNotFoundException.class);
    }
}
