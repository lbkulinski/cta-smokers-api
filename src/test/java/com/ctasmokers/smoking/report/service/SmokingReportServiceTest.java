package com.ctasmokers.smoking.report.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.exception.InvalidStationIdException;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import com.ctasmokers.smoking.report.model.SmokingReportLimits;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository;
import com.ctasmokers.smoking.report.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmokingReportServiceTest {
    private static final long EXPIRE_AFTER_MINUTES = 30L;
    private static final String CURSOR = "c2FtcGxlLWN1cnNvcg";

    @Mock
    private StationRepository stationRepository;

    @Mock
    private SmokingReportRepository repository;

    private SmokingReportService service;

    @BeforeEach
    void setUp() {
        var reportProperties = new CtaReportProperties("https://api.ctasmokers.com", EXPIRE_AFTER_MINUTES);

        service = new SmokingReportService(stationRepository, repository, reportProperties);
    }

    private SmokingReport report(long expiresAt) {
        return SmokingReport.builder()
                            .carNumberLine("2435#RED")
                            .carNumber("2435")
                            .line(TrainLine.RED)
                            .gsiPk(SmokingReportSchema.GSI_PK_VALUE)
                            .reportedAt(Instant.now())
                            .expiresAt(expiresAt)
                            .destinationId("40900")
                            .nextStationId("41220")
                            .build();
    }

    @Test
    void submitReport_savesReportAndReturnsIt() {
        when(stationRepository.existsByMapId("40900")).thenReturn(true);
        when(stationRepository.existsByMapId("41220")).thenReturn(true);

        long before = Instant.now().plusSeconds(EXPIRE_AFTER_MINUTES * 60L).getEpochSecond();

        SmokingReport response = service.submitReport("2435", TrainLine.RED, "40900", "41220", null);

        long after = Instant.now().plusSeconds(EXPIRE_AFTER_MINUTES * 60L).getEpochSecond();

        ArgumentCaptor<SmokingReport> reportCaptor = ArgumentCaptor.forClass(SmokingReport.class);
        verify(repository).save(reportCaptor.capture());

        SmokingReport saved = reportCaptor.getValue();
        assertThat(saved).isSameAs(response);
        assertThat(saved.carNumberLine()).isEqualTo("2435#RED");
        assertThat(saved.carNumber()).isEqualTo("2435");
        assertThat(saved.line()).isEqualTo(TrainLine.RED);
        assertThat(saved.gsiPk()).isEqualTo(SmokingReportSchema.GSI_PK_VALUE);
        assertThat(saved.destinationId()).isEqualTo("40900");
        assertThat(saved.nextStationId()).isEqualTo("41220");
        assertThat(saved.runNumber()).isNull();
        assertThat(saved.expiresAt()).isBetween(before, after);
        assertThat(saved.reportedAt().getEpochSecond() + EXPIRE_AFTER_MINUTES * 60L).isEqualTo(saved.expiresAt());
    }

    @Test
    void submitReport_withRunNumber_includesRunNumber() {
        when(stationRepository.existsByMapId("40900")).thenReturn(true);
        when(stationRepository.existsByMapId("41220")).thenReturn(true);

        service.submitReport("2435", TrainLine.BLUE, "40900", "41220", "902");

        ArgumentCaptor<SmokingReport> reportCaptor = ArgumentCaptor.forClass(SmokingReport.class);
        verify(repository).save(reportCaptor.capture());

        assertThat(reportCaptor.getValue().runNumber()).isEqualTo("902");
        assertThat(reportCaptor.getValue().carNumberLine()).isEqualTo("2435#BLUE");
    }

    @Test
    void submitReport_unknownDestination_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> service.submitReport("2435", TrainLine.RED, "40900", "41220", null))
            .isInstanceOf(InvalidStationIdException.class)
            .hasMessageContaining("destinationId")
            .hasMessageContaining("40900");

        verify(stationRepository, never()).existsByMapId("41220");
        verify(repository, never()).save(any());
    }

    @Test
    void submitReport_unknownNextStation_throwsAndDoesNotSave() {
        when(stationRepository.existsByMapId("40900")).thenReturn(true);

        assertThatThrownBy(() -> service.submitReport("2435", TrainLine.RED, "40900", "41220", null))
            .isInstanceOf(InvalidStationIdException.class)
            .hasMessageContaining("nextStationId")
            .hasMessageContaining("41220");

        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = TrainLine.class, names = {"BROWN", "ORANGE", "PURPLE", "PINK"})
    void submitReport_loopDestinationOnLoopLine_savesWithoutLookingUpDestination(TrainLine line) {
        when(stationRepository.existsByMapId("41220")).thenReturn(true);

        SmokingReport response = service.submitReport("2435", line, "0", "41220", null);

        assertThat(response.destinationId()).isEqualTo("0");
        assertThat(response.line()).isEqualTo(line);

        verify(stationRepository, never()).existsByMapId("0");
        verify(repository).save(response);
    }

    @ParameterizedTest
    @EnumSource(value = TrainLine.class, names = {"BROWN", "ORANGE", "PURPLE", "PINK"}, mode = EnumSource.Mode.EXCLUDE)
    void submitReport_loopDestinationOnNonLoopLine_throwsWithoutLookingUpDestination(TrainLine line) {
        assertThatThrownBy(() -> service.submitReport("2435", line, "0", "41220", null))
            .isInstanceOf(InvalidStationIdException.class)
            .hasMessageContaining("destinationId")
            .hasMessageContaining("0");

        verify(stationRepository, never()).existsByMapId(any());
        verify(repository, never()).save(any());
    }

    @Test
    void submitReport_loopAsNextStation_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> service.submitReport("2435", TrainLine.BROWN, "0", "0", null))
            .isInstanceOf(InvalidStationIdException.class)
            .hasMessageContaining("nextStationId");

        verify(repository, never()).save(any());
    }

    @Test
    void submitReport_whenRepositoryRejectsDuplicate_propagatesException() {
        when(stationRepository.existsByMapId("40900")).thenReturn(true);
        when(stationRepository.existsByMapId("41220")).thenReturn(true);
        doThrow(new SmokingReportAlreadyExistsException("2435", TrainLine.RED)).when(repository).save(any());

        assertThatThrownBy(() -> service.submitReport("2435", TrainLine.RED, "40900", "41220", null))
            .isInstanceOf(SmokingReportAlreadyExistsException.class);
    }

    @Test
    void getActiveReports_nullLimit_usesDefaultLimit() {
        var page = new SmokingReportPage(List.of(report(Long.MAX_VALUE)), null);

        when(repository.findActivePage(SmokingReportLimits.DEFAULT_LIMIT, null)).thenReturn(page);

        SmokingReportPage response = service.getActiveReports(null, null);

        assertThat(response).isSameAs(page);
    }

    @Test
    void getActiveReports_withLimitAndCursor_forwardsBothToRepository() {
        var page = new SmokingReportPage(List.of(), CURSOR);

        when(repository.findActivePage(5, CURSOR)).thenReturn(page);

        SmokingReportPage response = service.getActiveReports(5, CURSOR);

        assertThat(response.nextCursor()).isEqualTo(CURSOR);
        verify(repository).findActivePage(5, CURSOR);
    }

    @Test
    void getReportByCarNumberLine_activeReport_returnsIt() {
        SmokingReport report = report(Instant.now().getEpochSecond() + 600L);

        when(repository.findByCarNumberLine("2435", TrainLine.RED)).thenReturn(report);

        SmokingReport response = service.getReportByCarNumberLine("2435", TrainLine.RED);

        assertThat(response).isSameAs(report);
    }

    @Test
    void getReportByCarNumberLine_missing_throwsNotFound() {
        when(repository.findByCarNumberLine("2435", TrainLine.RED)).thenReturn(null);

        assertThatThrownBy(() -> service.getReportByCarNumberLine("2435", TrainLine.RED))
            .isInstanceOf(SmokingReportNotFoundException.class);
    }

    @Test
    void getReportByCarNumberLine_expiredReportStillInTable_throwsNotFound() {
        SmokingReport expired = report(Instant.now().getEpochSecond() - 1L);

        when(repository.findByCarNumberLine("2435", TrainLine.RED)).thenReturn(expired);

        assertThatThrownBy(() -> service.getReportByCarNumberLine("2435", TrainLine.RED))
            .isInstanceOf(SmokingReportNotFoundException.class);
    }
}
