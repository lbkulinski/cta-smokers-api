package com.ctasmokers.smoking.count.controller;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.smoking.count.dto.SmokingReportCountDto;
import com.ctasmokers.smoking.count.dto.SmokingReportDailyCountDto;
import com.ctasmokers.smoking.count.dto.SmokingReportDailyCountsDto;
import com.ctasmokers.smoking.count.model.SmokingReportDailyCount;
import com.ctasmokers.smoking.count.service.SmokingReportCountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/cta/reports/smoking/counts")
@Tag(
    name = "Smoking Report Counts",
    description = """
        Retrieve smoking report counts for CTA train lines. Days, months, and years use Chicago time \
        (America/Chicago), and weeks are ISO weeks."""
)
@NullMarked
public final class SmokingReportCountController {
    private final SmokingReportCountService countService;

    @Autowired
    public SmokingReportCountController(SmokingReportCountService countService) {
        this.countService = countService;
    }

    @GetMapping("/{line}/day/{date}")
    @Operation(
        summary = "Get count for a day",
        description = "Retrieve the smoking report count for a train line on a specific day"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report count retrieved successfully; 0 if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportCountDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line or date",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportCountDto> getDayCount(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line,

        @PathVariable
        @Parameter(description = "Day to retrieve the count for", example = "2026-02-21")
        LocalDate date
    ) {
        long reportCount = this.countService.getDayCount(line, date);

        var countDto = new SmokingReportCountDto(reportCount);

        return ResponseEntity.ok(countDto);
    }

    @GetMapping("/{line}/week/{yearWeek}")
    @Operation(
        summary = "Get count for an ISO week",
        description = "Retrieve the smoking report count for a train line in a specific ISO week"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report count retrieved successfully; 0 if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportCountDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line or week",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportCountDto> getWeekCount(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line,

        @PathVariable
        @Parameter(
            description = """
                ISO week to retrieve the count for. Parsed leniently: a lowercase w is accepted, and week 53 of a \
                year with only 52 weeks is read as week 1 of the next year (2025-W53 is read as 2026-W01)""",
            example = "2026-W13"
        )
        YearWeek yearWeek
    ) {
        long reportCount = this.countService.getWeekCount(line, yearWeek);

        var countDto = new SmokingReportCountDto(reportCount);

        return ResponseEntity.ok(countDto);
    }

    @GetMapping("/{line}/month/{yearMonth}")
    @Operation(
        summary = "Get count for a month",
        description = "Retrieve the smoking report count for a train line in a specific month"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report count retrieved successfully; 0 if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportCountDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line or month",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportCountDto> getMonthCount(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line,

        @PathVariable
        @Parameter(description = "Month to retrieve the count for", example = "2026-03")
        YearMonth yearMonth
    ) {
        long reportCount = this.countService.getMonthCount(line, yearMonth);

        var countDto = new SmokingReportCountDto(reportCount);

        return ResponseEntity.ok(countDto);
    }

    @GetMapping("/{line}/year/{year}")
    @Operation(
        summary = "Get count for a year",
        description = "Retrieve the smoking report count for a train line in a specific year"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report count retrieved successfully; 0 if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportCountDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line or year",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportCountDto> getYearCount(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line,

        @PathVariable
        @Parameter(description = "Year to retrieve the count for", example = "2026")
        Year year
    ) {
        long reportCount = this.countService.getYearCount(line, year);

        var countDto = new SmokingReportCountDto(reportCount);

        return ResponseEntity.ok(countDto);
    }

    @GetMapping("/{line}/all-time")
    @Operation(
        summary = "Get all-time count",
        description = "Retrieve the all-time smoking report count for a train line"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Report count retrieved successfully; 0 if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportCountDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportCountDto> getAllTimeCount(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line
    ) {
        long reportCount = this.countService.getAllTimeCount(line);

        var countDto = new SmokingReportCountDto(reportCount);

        return ResponseEntity.ok(countDto);
    }

    @GetMapping("/{line}/month/{yearMonth}/days")
    @Operation(
        summary = "Get daily counts for a month",
        description = "Retrieve the daily smoking report counts for a train line in a specific month"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Daily counts retrieved successfully; empty if no reports were recorded",
            content = @Content(
                schema = @Schema(implementation = SmokingReportDailyCountsDto.class),
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid line or month",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportDailyCountsDto> getDailyCounts(
        @PathVariable
        @Parameter(description = "CTA train line")
        TrainLine line,

        @PathVariable
        @Parameter(description = "Month to retrieve daily counts for", example = "2026-03")
        YearMonth yearMonth
    ) {
        List<SmokingReportDailyCount> dailyCounts = this.countService.getDailyCounts(line, yearMonth);

        var days = dailyCounts.stream()
                              .map(SmokingReportDailyCountDto::from)
                              .toList();

        var dailyCountsDto = new SmokingReportDailyCountsDto(days);

        return ResponseEntity.ok(dailyCountsDto);
    }
}
