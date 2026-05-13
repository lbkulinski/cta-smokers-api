package com.ctasmokers.smoking.aggregate.controller;

import com.ctasmokers.smoking.aggregate.dto.SmokingReportAggregateResponse;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCountsResponse;
import com.ctasmokers.smoking.aggregate.service.SmokingReportAggregateService;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/cta/reports/smoking/aggregates")
@Tag(name = "Smoking Report Aggregates", description = "Retrieve smoking report aggregates for CTA train lines")
@NullMarked
public final class SmokingReportAggregateController {
    private final SmokingReportAggregateService smokingReportAggregateService;

    @Autowired
    public SmokingReportAggregateController(SmokingReportAggregateService smokingReportAggregateService) {
        this.smokingReportAggregateService = smokingReportAggregateService;
    }

    @GetMapping("/{line}/day/{date}")
    @Operation(
        summary = "Get daily aggregate",
        description = "Retrieve the smoking report aggregate for a train line on a specific day"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Aggregate retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportAggregateResponse.class),
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
            description = "Aggregate not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportAggregateResponse> getDayAggregate(
        @PathVariable TrainLine line,
        @PathVariable LocalDate date
    ) {
        SmokingReportAggregateResponse response = this.smokingReportAggregateService.getDayAggregate(line, date);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{line}/week/{yearWeek}")
    @Operation(
        summary = "Get weekly aggregate",
        description = "Retrieve the smoking report aggregate for a train line in a specific ISO week (e.g. 2026-W13)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Aggregate retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportAggregateResponse.class),
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
            description = "Aggregate not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportAggregateResponse> getWeekAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-W13") YearWeek yearWeek
    ) {
        SmokingReportAggregateResponse response = this.smokingReportAggregateService.getWeekAggregate(line, yearWeek);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{line}/month/{yearMonth}")
    @Operation(
        summary = "Get monthly aggregate",
        description = "Retrieve the smoking report aggregate for a train line in a specific month (e.g. 2026-03)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Aggregate retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportAggregateResponse.class),
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
            description = "Aggregate not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportAggregateResponse> getMonthAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-03") YearMonth yearMonth
    ) {
        SmokingReportAggregateResponse response = this.smokingReportAggregateService.getMonthAggregate(line, yearMonth);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{line}/year/{year}")
    @Operation(
        summary = "Get yearly aggregate",
        description = "Retrieve the smoking report aggregate for a train line in a specific year (e.g. 2026)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Aggregate retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportAggregateResponse.class),
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
            description = "Aggregate not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportAggregateResponse> getYearAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026") Year year
    ) {
        SmokingReportAggregateResponse response = this.smokingReportAggregateService.getYearAggregate(line, year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{line}/all-time")
    @Operation(
        summary = "Get all-time aggregate",
        description = "Retrieve the all-time smoking report aggregate for a train line"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Aggregate retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportAggregateResponse.class),
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
            description = "Aggregate not found",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class),
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            )
        )
    })
    public ResponseEntity<SmokingReportAggregateResponse> getAllTimeAggregate(@PathVariable TrainLine line) {
        SmokingReportAggregateResponse response = this.smokingReportAggregateService.getAllTimeAggregate(line);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{line}/month/{yearMonth}/days")
    @Operation(
        summary = "Get daily counts",
        description = "Retrieve the daily smoking report counts for a train line in a specific month (e.g. 2026-03)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Counts retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = SmokingReportDailyCountsResponse.class),
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
    public ResponseEntity<SmokingReportDailyCountsResponse> getDailyCounts(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-03") YearMonth yearMonth
    ) {
        SmokingReportDailyCountsResponse response = this.smokingReportAggregateService.getDailyCounts(
            line,
            yearMonth
        );

        return ResponseEntity.ok(response);
    }
}
