package com.ctasmokers.smoking.aggregate.controller;

import com.ctasmokers.smoking.aggregate.dto.SmokingReportAggregateDto;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCountDto;
import com.ctasmokers.smoking.aggregate.dto.SmokingReportDailyCountsDto;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.aggregate.model.SmokingReportDailyCount;
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
import java.util.List;

@RestController
@RequestMapping("/api/cta/reports/smoking/aggregates")
@Tag(name = "Smoking Report Aggregates", description = "Retrieve smoking report aggregates for CTA train lines")
@NullMarked
public final class SmokingReportAggregateController {
    private final SmokingReportAggregateService aggregateService;

    @Autowired
    public SmokingReportAggregateController(SmokingReportAggregateService aggregateService) {
        this.aggregateService = aggregateService;
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
                schema = @Schema(implementation = SmokingReportAggregateDto.class),
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
    public ResponseEntity<SmokingReportAggregateDto> getDayAggregate(
        @PathVariable TrainLine line,
        @PathVariable LocalDate date
    ) {
        SmokingReportAggregate aggregate = this.aggregateService.getDayAggregate(line, date);

        SmokingReportAggregateDto aggregateDto = SmokingReportAggregateDto.from(aggregate);

        return ResponseEntity.ok(aggregateDto);
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
                schema = @Schema(implementation = SmokingReportAggregateDto.class),
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
    public ResponseEntity<SmokingReportAggregateDto> getWeekAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-W13") YearWeek yearWeek
    ) {
        SmokingReportAggregate aggregate = this.aggregateService.getWeekAggregate(line, yearWeek);

        SmokingReportAggregateDto aggregateDto = SmokingReportAggregateDto.from(aggregate);

        return ResponseEntity.ok(aggregateDto);
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
                schema = @Schema(implementation = SmokingReportAggregateDto.class),
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
    public ResponseEntity<SmokingReportAggregateDto> getMonthAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-03") YearMonth yearMonth
    ) {
        SmokingReportAggregate aggregate = this.aggregateService.getMonthAggregate(line, yearMonth);

        SmokingReportAggregateDto aggregateDto = SmokingReportAggregateDto.from(aggregate);

        return ResponseEntity.ok(aggregateDto);
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
                schema = @Schema(implementation = SmokingReportAggregateDto.class),
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
    public ResponseEntity<SmokingReportAggregateDto> getYearAggregate(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026") Year year
    ) {
        SmokingReportAggregate aggregate = this.aggregateService.getYearAggregate(line, year);

        SmokingReportAggregateDto aggregateDto = SmokingReportAggregateDto.from(aggregate);

        return ResponseEntity.ok(aggregateDto);
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
                schema = @Schema(implementation = SmokingReportAggregateDto.class),
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
    public ResponseEntity<SmokingReportAggregateDto> getAllTimeAggregate(@PathVariable TrainLine line) {
        SmokingReportAggregate aggregate = this.aggregateService.getAllTimeAggregate(line);

        SmokingReportAggregateDto aggregateDto = SmokingReportAggregateDto.from(aggregate);

        return ResponseEntity.ok(aggregateDto);
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
                schema = @Schema(implementation = SmokingReportDailyCountsDto.class),
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
    public ResponseEntity<SmokingReportDailyCountsDto> getDailyCounts(
        @PathVariable TrainLine line,
        @PathVariable @Schema(type = "string", example = "2026-03") YearMonth yearMonth
    ) {
        List<SmokingReportDailyCount> dailyCounts = this.aggregateService.getDailyCounts(
            line,
            yearMonth
        );

        List<SmokingReportDailyCountDto> days = dailyCounts.stream()
                                                           .map(SmokingReportDailyCountDto::from)
                                                           .toList();

        SmokingReportDailyCountsDto dailyCountsDto = new SmokingReportDailyCountsDto(days);

        return ResponseEntity.ok(dailyCountsDto);
    }
}
