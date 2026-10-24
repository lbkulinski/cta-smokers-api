package com.ctasmokers.smoking.report.controller;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.dto.SmokingReportDto;
import com.ctasmokers.smoking.report.dto.SmokingReportPageDto;
import com.ctasmokers.smoking.report.dto.SubmitReportDto;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportLimits;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import com.ctasmokers.smoking.report.service.SmokingReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.NullMarked;
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

import java.util.List;

@RestController
@RequestMapping("/api/cta/reports/smoking")
@Tag(name = "Smoking Reports", description = "Submit and retrieve smoking reports on CTA trains")
@NullMarked
public final class SmokingReportController {
    private static final String LOCATION_HEADER_FORMAT = "%s/api/cta/reports/smoking/{carNumber}/{line}";

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
    @Operation(
        summary = "Submit a new smoking report for a CTA train",
        description = """
        Create a new smoking report for the given car and line, rejecting duplicates while an active report already \
        exists for that car and line"""
    )
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
            description = "Invalid request data, such as an unknown station ID",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Report already exists",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportDto> submitReport(@Valid @RequestBody SubmitReportDto request) {
        SmokingReport report = this.reportService.submitReport(
            request.carNumber(),
            request.line(),
            request.destinationId(),
            request.nextStationId(),
            request.runNumber());

        var reportDto = SmokingReportDto.from(report);

        var location = UriComponentsBuilder.fromUriString(LOCATION_HEADER_FORMAT.formatted(this.baseUrl))
                                           .buildAndExpand(report.carNumber(), report.line())
                                           .encode()
                                           .toUri();

        return ResponseEntity.created(location).body(reportDto);
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active smoking reports",
        description = "Retrieve a page of active smoking reports, newest first"
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
            description = "Invalid limit or cursor",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportPageDto> getActiveReports(
        @RequestParam(required = false)
        @Parameter(description = "Maximum number of reports to return (defaults to 25)")
        @Min(1)
        @Max(SmokingReportLimits.MAX_LIMIT)
        Integer limit,

        @RequestParam(required = false)
        @Parameter(description = "The nextCursor value from the previous page, passed back unchanged")
        String nextCursor
    ) {
        SmokingReportPage page = this.reportService.getActiveReports(limit, nextCursor);

        List<SmokingReportDto> reportsDtos = page.reports()
                                                 .stream()
                                                 .map(SmokingReportDto::from)
                                                 .toList();

        var pageDto = new SmokingReportPageDto(reportsDtos, page.nextCursor());

        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/{carNumber}/{line}")
    @Operation(
        summary = "Get the active smoking report for a car number and line",
        description = "Retrieve the active smoking report for a car number and line"
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
            description = "No active report found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportDto> getReportByCarNumberLine(
        @PathVariable
        @Parameter(description = "Car number of the report to retrieve (e.g. 2435)")
        String carNumber,

        @PathVariable
        @Parameter(description = "CTA train line")
        @Schema(type = "string", example = "RED")
        TrainLine line
    ) {
        SmokingReport report = this.reportService.getReportByCarNumberLine(carNumber, line);

        var reportDto = SmokingReportDto.from(report);

        return ResponseEntity.ok(reportDto);
    }
}
