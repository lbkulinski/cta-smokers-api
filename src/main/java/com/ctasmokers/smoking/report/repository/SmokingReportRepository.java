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
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

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

    private static final String REPORT_DATE_KEY = "reportDate";
    private static final String EXPIRES_AT_KEY = "expiresAt";

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

    public SmokingReportPage findActivePageByReportedDate(
        LocalDate reportedDate,
        int pageSize,
        @Nullable String nextCursor
    ) {
        Objects.requireNonNull(reportedDate);

        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "pageSize must be a positive integer between %d and %d".formatted(MIN_PAGE_SIZE, MAX_PAGE_SIZE));
        }

        var reportDateString = reportedDate.toString();

        var key = Key.builder()
                     .partitionValue(reportDateString)
                     .build();

        var queryConditional = QueryConditional.keyEqualTo(key);

        long now = Instant.now().getEpochSecond();
        var nowString = Long.toString(now);

        var activeExpression = Expression.builder()
                                         .expression("expiresAt > :now")
                                         .expressionValues(Map.of("now", AttributeValue.createN(nowString)))
                                         .build();

        Map<String, AttributeValue> exclusiveStartKey = null;

        if (nextCursor != null) {
            exclusiveStartKey = Map.of(
                REPORT_DATE_KEY, AttributeValue.builder().s(reportDateString).build(),
                EXPIRES_AT_KEY, AttributeValue.builder().s(nextCursor).build()
            );
        }

        var request = QueryEnhancedRequest.builder()
                                          .queryConditional(queryConditional)
                                          .filterExpression(activeExpression)
                                          .limit(pageSize)
                                          .exclusiveStartKey(exclusiveStartKey)
                                          .scanIndexForward(false)
                                          .build();

        return this.smokingReports.index(SmokingReport.REPORT_DATE_EXPIRES_AT_INDEX)
                                  .query(request)
                                  .stream()
                                  .findFirst()
                                  .map(page -> new SmokingReportPage(
                                      page.items() == null
                                          ? List.of()
                                          : List.copyOf(page.items()),
                                      page.lastEvaluatedKey() == null
                                          ? null
                                          : page.lastEvaluatedKey().get(EXPIRES_AT_KEY).s()
                                  ))
                                  .orElseGet(() -> new SmokingReportPage(List.of(), null));
    }

    public @Nullable SmokingReport findByCarNumberLine(String carNumber, TrainLine line) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);

        var key = Key.builder()
                     .partitionValue(carNumber)
                     .sortValue(line.name())
                     .build();

        return this.smokingReports.getItem(key);
    }
}
