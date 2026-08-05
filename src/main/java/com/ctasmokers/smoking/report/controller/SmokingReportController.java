package com.ctasmokers.smoking.report.controller;

import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.dto.SmokingReportDto;
import com.ctasmokers.smoking.report.dto.SmokingReportPageDto;
import com.ctasmokers.smoking.report.dto.SubmitReportDto;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.service.SmokingReportService;
import com.ctasmokers.smoking.report.validator.ValidReportId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cta/reports/smoking")
@Tag(name = "Smoking Reports", description = "Submit and retrieve smoking reports on CTA trains")
@NullMarked
public final class SmokingReportController {
    private static final String LOCATION_HEADER_FORMAT = "%s/api/cta/reports/smoking/{date}/{reportId}";

    private final SmokingReportService reportService;

    private final String baseUrl;

    @Autowired
    public SmokingReportController(
        SmokingReportService reportService,
        CtaReportProperties reportProperties
    ) {
        this.reportService = reportService;
        this.baseUrl = reportProperties.baseUrl();
    }

    @PostMapping
    @Operation(summary = "Submit a new smoking report for a CTA train")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Report created successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportDto.class),
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
    public ResponseEntity<SmokingReportDto> submitReport(@Valid @RequestBody SubmitReportDto request) {
        SmokingReport report = this.reportService.submitReport(
            request.line(),
            request.destinationId(),
            request.nextStationId(),
            request.carNumber(),
            request.runNumber()
        );

        SmokingReportDto reportDto = SmokingReportDto.from(report);

        URI location = UriComponentsBuilder.fromUriString(LOCATION_HEADER_FORMAT.formatted(this.baseUrl))
                                           .buildAndExpand(report.date(), report.reportId())
                                           .encode()
                                           .toUri();

        return ResponseEntity.created(location)
                             .body(reportDto);
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
                schema = @Schema(implementation = SmokingReportPageDto.class),
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
    public ResponseEntity<SmokingReportPageDto> getReportsByDate(
        @PathVariable LocalDate date,
        @Nullable @ValidReportId @RequestParam(required = false) String nextCursor
    ) {
        SmokingReportPage page = this.reportService.getReportsByDate(date, nextCursor);

        List<SmokingReportDto> reportsDtos = page.reports()
                                                 .stream()
                                                 .map(SmokingReportDto::from)
                                                 .toList();

        SmokingReportPageDto pageDto = new SmokingReportPageDto(reportsDtos, page.nextCursor());

        return ResponseEntity.ok(pageDto);
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
                schema = @Schema(implementation = SmokingReportDto.class),
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
    public ResponseEntity<SmokingReportDto> getReportById(
        @PathVariable LocalDate date,
        @PathVariable @ValidReportId String reportId
    ) {
        SmokingReport report = this.reportService.getReportById(date, reportId);

        SmokingReportDto reportDto = SmokingReportDto.from(report);

        return ResponseEntity.ok(reportDto);
    }
}
