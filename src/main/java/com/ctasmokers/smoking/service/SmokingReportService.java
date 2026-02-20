package com.ctasmokers.smoking.service;

import com.ctasmokers.smoking.dto.SaveReportRequest;
import com.ctasmokers.smoking.dto.SaveReportResponse;
import com.ctasmokers.smoking.model.SmokingReport;
import com.ctasmokers.smoking.model.TrainLine;
import com.ctasmokers.smoking.repository.SmokingReportRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    private final SmokingReportRepository smokingReportRepository;
    private final int pageSize;

    @Autowired
    public SmokingReportService(
        SmokingReportRepository smokingReportRepository,
        @Value("${app.aws.cta.reports.page-size}") int pageSize
    ) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be a positive integer");
        }

        this.smokingReportRepository = smokingReportRepository;
        this.pageSize = pageSize;
    }

    public SaveReportResponse saveReport(SaveReportRequest request) {
        Objects.requireNonNull(request);

        TrainLine line;

        try {
            line = TrainLine.valueOf(request.line());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Instant now = Instant.now();

        LocalDate date = now.atZone(CHICAGO_ZONE_ID)
                            .toLocalDate();

        long epochMillis = now.toEpochMilli();
        String uuid = UUID.randomUUID()
                          .toString();

        String reportId = "%s#%s".formatted(epochMillis, uuid);
        long expiresAt = now.plus(12L, ChronoUnit.HOURS)
                            .getEpochSecond();

        SmokingReport report = SmokingReport.builder()
                                            .date(date)
                                            .reportId(reportId)
                                            .reportedAt(now)
                                            .expiresAt(expiresAt)
                                            .line(line)
                                            .destination(request.destination())
                                            .nextStop(request.nextStop())
                                            .carNumber(request.carNumber())
                                            .runNumber(request.runNumber())
                                            .build();

        this.smokingReportRepository.save(report);

        return null;
    }

    public List<SmokingReport> findReportsByDate(LocalDate date, @Nullable String lastReportId) {
        Objects.requireNonNull(date);

        return this.smokingReportRepository.findPageByDate(date, this.pageSize, lastReportId);
    }
}
