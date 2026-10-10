package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@NullMarked
public final class SmokingReportRepository {
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private static final String DATE_KEY = "date";
    private static final String REPORT_ID_KEY = "reportId";

    private final DynamoDbTable<@Nullable SmokingReport> smokingReports;

    @Autowired
    public SmokingReportRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        DynamoDbProperties dynamoDbProperties
    ) {
        String tableName = dynamoDbProperties.smokingReports();
        var tableSchema = TableSchema.fromImmutableClass(SmokingReport.class);

        this.smokingReports = dynamoDbClient.table(tableName, tableSchema);
    }

    public void save(SmokingReport report) {
        Objects.requireNonNull(report);

        this.smokingReports.putItem(report);
    }

    public SmokingReportPage findPageByDate(LocalDate date, int pageSize, @Nullable String nextCursor) {
        Objects.requireNonNull(date);

        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "pageSize must be a positive integer between %d and %d".formatted(MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }

        var dateString = date.toString();

        var key = Key.builder()
                     .partitionValue(dateString)
                     .build();

        var queryConditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> exclusiveStartKey = null;

        if (nextCursor != null) {
            exclusiveStartKey = Map.of(
                DATE_KEY, AttributeValue.builder().s(dateString).build(),
                REPORT_ID_KEY, AttributeValue.builder().s(nextCursor).build()
            );
        }

        var request = QueryEnhancedRequest.builder()
                                          .queryConditional(queryConditional)
                                          .limit(pageSize)
                                          .exclusiveStartKey(exclusiveStartKey)
                                          .scanIndexForward(false)
                                          .build();

        return this.smokingReports.query(request)
                                  .stream()
                                  .findFirst()
                                  .map(page -> new SmokingReportPage(
                                      page.items() == null
                                          ? List.of()
                                          : List.copyOf(page.items()),
                                      page.lastEvaluatedKey() == null
                                          ? null
                                          : page.lastEvaluatedKey().get(REPORT_ID_KEY).s()
                                  ))
                                  .orElseGet(() -> new SmokingReportPage(List.of(), null));
    }

    public @Nullable SmokingReport findById(LocalDate date, String reportId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);

        var key = Key.builder()
                     .partitionValue(date.toString())
                     .sortValue(reportId)
                     .build();

        return this.smokingReports.getItem(key);
    }

    public boolean existsActiveByCarNumberAndLine(String carNumber, TrainLine line) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);

        String carNumberLine = SmokingReport.carNumberLineOf(carNumber, line);
        long now = Instant.now().getEpochSecond();

        var key = Key.builder()
                     .partitionValue(carNumberLine)
                     .sortValue(now)
                     .build();

        var queryConditional = QueryConditional.sortGreaterThanOrEqualTo(key);

        var request = QueryEnhancedRequest.builder()
                                          .select(Select.COUNT)
                                          .queryConditional(queryConditional)
                                          .limit(1)
                                          .build();

        return this.smokingReports.index(SmokingReport.CAR_NUMBER_LINE_EXPIRES_AT_INDEX)
                                  .query(request)
                                  .stream()
                                  .mapToInt(Page::count)
                                  .sum() > 0;
    }
}
