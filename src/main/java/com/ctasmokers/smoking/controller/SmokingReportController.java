package com.ctasmokers.smoking.controller;

import com.ctasmokers.smoking.dto.SmokingReportResponse;
import com.ctasmokers.smoking.dto.SmokingReportsResponse;
import com.ctasmokers.smoking.dto.SubmitReportRequest;
import com.ctasmokers.smoking.dto.SubmitReportResponse;
import com.ctasmokers.smoking.service.SmokingReportService;
import com.ctasmokers.smoking.validator.ValidReportId;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cta/reports/smoking")
public final class SmokingReportController {
    private final SmokingReportService smokingReportService;

    @Autowired
    public SmokingReportController(SmokingReportService smokingReportService) {
        this.smokingReportService = smokingReportService;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Report created successfully")
    public ResponseEntity<SubmitReportResponse> submitReport(@Valid @RequestBody SubmitReportRequest request) {
        return this.smokingReportService.submitReport(request);
    }

    @GetMapping("/{date}")
    public ResponseEntity<SmokingReportsResponse> getReportsByDate(
        @PathVariable LocalDate date,
        @Nullable @ValidReportId @RequestParam String nextCursor
    ) {
        return this.smokingReportService.getReportsByDate(date, nextCursor);
    }

    @GetMapping("/{date}/{reportId}")
    public ResponseEntity<SmokingReportResponse> getReportById(
        @PathVariable LocalDate date,
        @PathVariable @ValidReportId String reportId
    ) {
        return this.smokingReportService.getReportById(date, reportId);
    }
}
