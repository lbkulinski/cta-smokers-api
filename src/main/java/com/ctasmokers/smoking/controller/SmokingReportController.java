package com.ctasmokers.smoking.controller;

import com.ctasmokers.smoking.dto.SubmitReportRequest;
import com.ctasmokers.smoking.dto.SubmitReportResponse;
import com.ctasmokers.smoking.service.SmokingReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cta/smoking/reports")
public final class SmokingReportController {
    private final SmokingReportService smokingReportService;

    @Autowired
    public SmokingReportController(SmokingReportService smokingReportService) {
        this.smokingReportService = smokingReportService;
    }

    @PostMapping
    public SubmitReportResponse submitReport(@Valid @RequestBody SubmitReportRequest request) {
        return this.smokingReportService.submitReport(request);
    }
}
