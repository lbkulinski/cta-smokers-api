package com.ctasmokers.smoking.report.service;

import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Service
@NullMarked
public final class SmokingReportService {
    private static final ZoneId CHICAGO_ZONE_ID = ZoneId.of("America/Chicago");
    private static final String REPORT_ID_FORMAT = "%d_%s";

    private final SmokingReportRepository reportRepository;

    private final int pageSize;
    private final long expireAfterMinutes;

    @Autowired
    public SmokingReportService(
        SmokingReportRepository reportRepository,
        CtaReportProperties reportsProperties
    ) {
        this.reportRepository = reportRepository;
        this.pageSize = reportsProperties.pageSize();
        this.expireAfterMinutes = reportsProperties.expireAfterMinutes();
    }

    public SmokingReport submitReport(
        String line,
        String destinationId,
        String nextStationId,
        String carNumber,
        @Nullable String runNumber
    ) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);
        Objects.requireNonNull(carNumber);

        TrainLine trainLine = TrainLine.valueOf(line);

        if (this.reportRepository.existsActiveByCarNumberAndLine(carNumber, trainLine)) {
            throw new SmokingReportAlreadyExistsException(carNumber, trainLine);
        }

        Instant now = Instant.now();
        long epochMillis = now.toEpochMilli();
        String uuid = UUID.randomUUID().toString();

        LocalDate date = now.atZone(CHICAGO_ZONE_ID).toLocalDate();
        String reportId = REPORT_ID_FORMAT.formatted(epochMillis, uuid);

        long expiresAt = now.plus(this.expireAfterMinutes, ChronoUnit.MINUTES).getEpochSecond();
        String carNumberLine = SmokingReport.carNumberLineOf(carNumber, trainLine);

        SmokingReport report = SmokingReport.builder()
                                            .date(date)
                                            .reportId(reportId)
                                            .reportedAt(now)
                                            .expiresAt(expiresAt)
                                            .line(trainLine)
                                            .destinationId(destinationId)
                                            .nextStationId(nextStationId)
                                            .carNumber(carNumber)
                                            .carNumberLine(carNumberLine)
                                            .runNumber(runNumber)
                                            .build();

        this.reportRepository.save(report);

        return report;
    }

    public SmokingReportPage getReportsByDate(LocalDate date, @Nullable String nextCursor) {
        Objects.requireNonNull(date);

        return this.reportRepository.findPageByDate(date, this.pageSize, nextCursor);
    }

    public SmokingReport getReportById(LocalDate date, String reportId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);

        return this.reportRepository.findById(date, reportId)
                                    .orElseThrow(() -> new SmokingReportNotFoundException(date, reportId));
    }
}
