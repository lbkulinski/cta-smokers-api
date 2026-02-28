package com.ctasmokers.smoking.controller;

import com.ctasmokers.smoking.dto.SmokingReportResponse;
import com.ctasmokers.smoking.dto.SmokingReportsResponse;
import com.ctasmokers.smoking.dto.SubmitReportRequest;
import com.ctasmokers.smoking.service.SmokingReportService;
import com.ctasmokers.smoking.validator.ValidReportId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Smoking Reports", description = "Submit and retrieve smoking reports on CTA trains")
@RestController
@RequestMapping("/api/cta/reports/smoking")
public final class SmokingReportController {
    private final SmokingReportService smokingReportService;

    @Autowired
    public SmokingReportController(SmokingReportService smokingReportService) {
        this.smokingReportService = smokingReportService;
    }

    @PostMapping
    @Operation(
        summary = "Submit a new smoking report",
        description = "Submit a new smoking report for a CTA train"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Report created successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportResponse.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportResponse> submitReport(@Valid @RequestBody SubmitReportRequest request) {
        return this.smokingReportService.submitReport(request);
    }

    @GetMapping("/{date}")
    @Operation(
        summary = "Get smoking reports by date",
        description = "Retrieve all smoking reports for a specific date"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reports retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportsResponse.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportsResponse> getReportsByDate(
        @PathVariable LocalDate date,
        @Nullable @ValidReportId @RequestParam String nextCursor
    ) {
        return this.smokingReportService.getReportsByDate(date, nextCursor);
    }

    @GetMapping("/{date}/{reportId}")
    @Operation(
        summary = "Get a smoking report by ID",
        description = "Retrieve a specific smoking report by its ID and date"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportResponse.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Report not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportResponse> getReportById(
        @PathVariable LocalDate date,
        @PathVariable @ValidReportId String reportId
    ) {
        return this.smokingReportService.getReportById(date, reportId);
    }
}
