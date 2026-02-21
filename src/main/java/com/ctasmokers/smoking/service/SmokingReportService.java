package com.ctasmokers.smoking.service;

import com.ctasmokers.smoking.dto.SmokingReportsResponse;
import com.ctasmokers.smoking.dto.SmokingReportResponse;
import com.ctasmokers.smoking.dto.SubmitReportRequest;
import com.ctasmokers.smoking.dto.SubmitReportResponse;
import com.ctasmokers.smoking.model.SmokingReport;
import com.ctasmokers.smoking.model.TrainLine;
import com.ctasmokers.smoking.repository.SmokingReportRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    private static final String REPORT_ID_FORMAT = "%d#%s";
    private static final String LOCATION_HEADER_FORMAT = "/api/cta/smoking/reports/{date}/{reportId}";

    private final SmokingReportRepository smokingReportRepository;

    private final int pageSize;
    private final long expireAfterHours;

    @Autowired
    public SmokingReportService(
        SmokingReportRepository smokingReportRepository,
        @Value("${app.aws.cta.reports.page-size}") int pageSize,
        @Value("${app.aws.cta.reports.expire-after-hours}") int expireAfterHours
    ) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be a positive integer");
        }

        if (expireAfterHours <= 0) {
            throw new IllegalArgumentException("expireAfterHours must be a positive integer");
        }

        this.smokingReportRepository = smokingReportRepository;
        this.pageSize = pageSize;
        this.expireAfterHours = expireAfterHours;
    }

    public ResponseEntity<SubmitReportResponse> submitReport(SubmitReportRequest request) {
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

        String reportId = REPORT_ID_FORMAT.formatted(epochMillis, uuid);
        long expiresAt = now.plus(this.expireAfterHours, ChronoUnit.HOURS)
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

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                                  .path(LOCATION_HEADER_FORMAT)
                                                  .buildAndExpand(date, reportId)
                                                  .encode()
                                                  .toUri();

        SubmitReportResponse response = SubmitReportResponse.from(report);

        return ResponseEntity.created(location)
                             .body(response);
    }

    public ResponseEntity<SmokingReportsResponse> getReportsByDate(LocalDate date, @Nullable String nextCursor) {
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

        if (reportResponses.isEmpty() || (page.lastEvaluatedKey() == null)) {
            newCursor = null;
        } else {
            newCursor = page.lastEvaluatedKey()
                            .get(SmokingReportRepository.REPORT_ID_KEY)
                            .s();
        }

        SmokingReportsResponse response = new SmokingReportsResponse(reportResponses, newCursor);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<SmokingReportResponse> getReportById(LocalDate date, String reportId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);

        return this.smokingReportRepository.findById(date, reportId)
                                           .map(SmokingReportResponse::from)
                                           .map(ResponseEntity::ok)
                                           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
