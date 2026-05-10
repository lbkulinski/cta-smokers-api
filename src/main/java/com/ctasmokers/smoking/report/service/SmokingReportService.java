package com.ctasmokers.smoking.report.service;

import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.dto.SmokingReportsResponse;
import com.ctasmokers.smoking.report.dto.SmokingReportResponse;
import com.ctasmokers.smoking.report.dto.SubmitReportRequest;
import com.ctasmokers.smoking.report.exception.SmokingReportNotFoundException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.repository.SmokingReportRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@NullMarked
public final class SmokingReportService {
    private static final ZoneId CHICAGO_ZONE_ID = ZoneId.of("America/Chicago");
    private static final String REPORT_ID_FORMAT = "%d_%s";

    private final SmokingReportRepository smokingReportRepository;

    private final int pageSize;
    private final long expireAfterMinutes;

    @Autowired
    public SmokingReportService(
        SmokingReportRepository smokingReportRepository,
        CtaReportProperties reportsProperties
    ) {
        this.smokingReportRepository = smokingReportRepository;
        this.pageSize = reportsProperties.pageSize();
        this.expireAfterMinutes = reportsProperties.expireAfterMinutes();
    }

    public SmokingReportResponse submitReport(SubmitReportRequest request) {
        Objects.requireNonNull(request);

        TrainLine line = TrainLine.valueOf(request.line());

        Instant now = Instant.now();

        LocalDate date = now.atZone(CHICAGO_ZONE_ID)
                            .toLocalDate();

        long epochMillis = now.toEpochMilli();
        String uuid = UUID.randomUUID()
                          .toString();

        String reportId = REPORT_ID_FORMAT.formatted(epochMillis, uuid);
        long expiresAt = now.plus(this.expireAfterMinutes, ChronoUnit.MINUTES)
                            .getEpochSecond();

        SmokingReport report = SmokingReport.builder()
                                            .date(date)
                                            .reportId(reportId)
                                            .reportedAt(now)
                                            .expiresAt(expiresAt)
                                            .line(line)
                                            .destinationId(request.destinationId())
                                            .nextStationId(request.nextStationId())
                                            .carNumber(request.carNumber())
                                            .runNumber(request.runNumber())
                                            .build();

        this.smokingReportRepository.save(report);

        return SmokingReportResponse.from(report);
    }

    public SmokingReportsResponse getReportsByDate(LocalDate date, @Nullable String nextCursor) {
        Objects.requireNonNull(date);

        SmokingReportRepository.SmokingReportPage page = this.smokingReportRepository.findPageByDate(
            date,
            this.pageSize,
            nextCursor
        );

        List<SmokingReportResponse> reportResponses = page.reports()
                                                          .stream()
                                                          .map(SmokingReportResponse::from)
                                                          .toList();

        String newCursor;

        if (page.lastEvaluatedKey() == null) {
            newCursor = null;
        } else {
            newCursor = page.lastEvaluatedKey()
                            .get(SmokingReportRepository.REPORT_ID_KEY)
                            .s();
        }

        return new SmokingReportsResponse(reportResponses, newCursor);
    }

    public SmokingReportResponse getReportById(LocalDate date, String reportId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);

        return this.smokingReportRepository.findById(date, reportId)
                                           .map(SmokingReportResponse::from)
                                           .orElseThrow(() -> new SmokingReportNotFoundException(date, reportId));
    }
}
