package com.ctasmokers.smoking.report.repository;

import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportPage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

@Repository
@NullMarked
public final class SmokingReportRepository {
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DATE_KEY = "date";
    private static final String REPORT_ID_KEY = "reportId";

    private final DynamoDbTable<SmokingReport> smokingReports;

    @Autowired
    public SmokingReportRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        @Value("${app.aws.dynamodb.tables.smoking-reports}") String tableName
    ) {
        TableSchema<SmokingReport> tableSchema = TableSchema.fromImmutableClass(SmokingReport.class);

        this.smokingReports = dynamoDbClient.table(tableName, tableSchema);
    }

    public void save(SmokingReport report) {
        Objects.requireNonNull(report);

        this.smokingReports.putItem(report);
    }

    public SmokingReportPage findPageByDate(
        LocalDate date,
        int pageSize,
        @Nullable String nextCursor
    ) {
        Objects.requireNonNull(date);

        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "pageSize must be a positive integer between %d and %d".formatted(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
            );
        }

        String dateString = date.toString();

        Key key = Key.builder()
                     .partitionValue(dateString)
                     .build();

        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> exclusiveStartKey = null;

        if (nextCursor != null) {
            exclusiveStartKey = Map.of(
                DATE_KEY, AttributeValue.builder().s(dateString).build(),
                REPORT_ID_KEY, AttributeValue.builder().s(nextCursor).build()
            );
        }

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                                                           .queryConditional(queryConditional)
                                                           .limit(pageSize)
                                                           .exclusiveStartKey(exclusiveStartKey)
                                                           .scanIndexForward(false)
                                                           .build();

        return this.smokingReports.query(request)
                                  .stream()
                                  .findFirst()
                                  .map(page -> new SmokingReportPage(
                                      page.items(),
                                      page.lastEvaluatedKey() == null
                                          ? null
                                          : page.lastEvaluatedKey().get(REPORT_ID_KEY).s()
                                  ))
                                  .orElseGet(() -> new SmokingReportPage(List.of(), null));
    }

    public Optional<SmokingReport> findById(LocalDate date, String reportId) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(reportId);

        Key key = Key.builder()
                     .partitionValue(date.toString())
                     .sortValue(reportId)
                     .build();

        SmokingReport report = this.smokingReports.getItem(key);

        return Optional.ofNullable(report);
    }

    public boolean existsActiveByCarNumberAndLine(String carNumber, TrainLine line) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);

        Key key = Key.builder()
                     .partitionValue(carNumber)
                     .sortValue(line.name())
                     .build();

        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);

        long now = Instant.now()
                          .getEpochSecond();

        AttributeValue nowValue = AttributeValue.builder()
                                                .n(Long.toString(now))
                                                .build();

        Expression filterExpression = Expression.builder()
                                                .expression("expiresAt > :now")
                                                .putExpressionValue(":now", nowValue)
                                                .build();

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                                                           .queryConditional(queryConditional)
                                                           .filterExpression(filterExpression)
                                                           .limit(1)
                                                           .build();

        return this.smokingReports.index(SmokingReport.CAR_NUMBER_LINE_INDEX)
                                  .query(request)
                                  .stream()
                                  .anyMatch(page -> !page.items().isEmpty());
    }
}
