package com.ctasmokers.smoking.report.service;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.exception.InvalidStationIdException;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportSchema;
import com.ctasmokers.smoking.report.model.SmokingReportLimits;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;

@Service
@NullMarked
public final class SmokingReportService {
    private final StationService stationService;
    private final SmokingReportRepository reportRepository;

    private final long expireAfterMinutes;

    @Autowired
    public SmokingReportService(
        StationService stationService,
        SmokingReportRepository reportRepository,
        CtaReportProperties reportProperties
    ) {
        this.stationService = stationService;
        this.reportRepository = reportRepository;
        this.expireAfterMinutes = reportProperties.expireAfterMinutes();
    }

    public SmokingReport submitReport(
        String carNumber,
        String line,
        String destinationId,
        String nextStationId,
        @Nullable String runNumber
    ) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);
        Objects.requireNonNull(destinationId);
        Objects.requireNonNull(nextStationId);

        Set<String> stationIds = this.stationService.getIds();

        if (!stationIds.contains(destinationId)) {
            throw new InvalidStationIdException("destinationId", destinationId);
        }

        if (!stationIds.contains(nextStationId)) {
            throw new InvalidStationIdException("nextStationId", nextStationId);
        }

        var trainLine = TrainLine.valueOf(line);

        String carNumberLine = SmokingReportSchema.carNumberLine(carNumber, trainLine);

        var now = Instant.now();

        long expiresAt = now.plus(this.expireAfterMinutes, ChronoUnit.MINUTES).getEpochSecond();

        var report = SmokingReport.builder()
                                  .carNumberLine(carNumberLine)
                                  .carNumber(carNumber)
                                  .line(trainLine)
                                  .gsiPk(SmokingReportSchema.GSI_PK_VALUE)
                                  .reportedAt(now)
                                  .expiresAt(expiresAt)
                                  .destinationId(destinationId)
                                  .nextStationId(nextStationId)
                                  .runNumber(runNumber)
                                  .build();

        this.reportRepository.save(report);

        return report;
    }

    public SmokingReportPage getActiveReports(
        @Nullable Integer limit,
        @Nullable String nextCursor
    ) {
        int resolvedLimit = limit == null
            ? SmokingReportLimits.DEFAULT_LIMIT
            : limit;

        return this.reportRepository.findActivePage(resolvedLimit, nextCursor);
    }

    public SmokingReport getReportByCarNumberLine(String carNumber, TrainLine line) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);

        SmokingReport report = this.reportRepository.findByCarNumberLine(carNumber, line);

        long now = Instant.now().getEpochSecond();

        if (report == null || report.expiresAt() <= now) {
            throw new SmokingReportNotFoundException(carNumber, line);
        }

        return report;
    }
}
