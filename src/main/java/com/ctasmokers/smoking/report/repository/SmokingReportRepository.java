package com.ctasmokers.smoking.report.repository;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.report.config.CtaReportProperties;
import com.ctasmokers.smoking.report.cursor.SmokingReportCursorCodec;
import com.ctasmokers.smoking.report.exception.SmokingReportAlreadyExistsException;
import com.ctasmokers.smoking.report.model.SmokingReport;
import com.ctasmokers.smoking.report.model.SmokingReportKeys;
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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@NullMarked
public final class SmokingReportRepository {
    private static final String NOW_PLACEHOLDER = ":now";
    private static final String NOT_EXISTS_OR_EXPIRED_EXPRESSION =
        "attribute_not_exists(carNumberLine) OR expiresAt < %s".formatted(NOW_PLACEHOLDER);

    private final DynamoDbTable<@Nullable SmokingReport> smokingReports;
    private final SmokingReportCursorCodec cursorCodec;

    private final int maxLimit;

    @Autowired
    public SmokingReportRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        SmokingReportCursorCodec cursorCodec,
        DynamoDbProperties dynamoDbProperties,
        CtaReportProperties reportProperties
    ) {
        String tableName = dynamoDbProperties.smokingReports();
        var tableSchema = TableSchema.fromImmutableClass(SmokingReport.class);

        this.smokingReports = dynamoDbClient.table(tableName, tableSchema);
        this.cursorCodec = cursorCodec;
        this.maxLimit = reportProperties.maxLimit();
    }

    public void save(SmokingReport report) {
        Objects.requireNonNull(report);

        long now = Instant.now().getEpochSecond();
        var nowString = Long.toString(now);

        var validExpression = Expression.builder()
                                        .expression(NOT_EXISTS_OR_EXPIRED_EXPRESSION)
                                        .expressionValues(Map.of(NOW_PLACEHOLDER, AttributeValue.createN(nowString)))
                                        .build();

        var request = PutItemEnhancedRequest.builder(SmokingReport.class)
                                            .item(report)
                                            .conditionExpression(validExpression)
                                            .build();

        try {
            this.smokingReports.putItem(request);
        } catch (ConditionalCheckFailedException e) {
            throw new SmokingReportAlreadyExistsException(report.carNumber(), report.line());
        }
    }

    public SmokingReportPage findActivePageByReportedDate(
        LocalDate reportedDate,
        int limit,
        @Nullable String nextCursor
    ) {
        Objects.requireNonNull(reportedDate);

        if (limit <= 0 || limit > this.maxLimit) {
            throw new IllegalArgumentException("limit must be between 1 and %d".formatted(this.maxLimit));
        }

        long now = Instant.now().getEpochSecond();

        var key = Key.builder()
                     .partitionValue(reportedDate.toString())
                     .sortValue(now)
                     .build();

        var queryConditional = QueryConditional.sortGreaterThan(key);

        Map<String, AttributeValue> exclusiveStartKey = null;

        if (nextCursor != null) {
            exclusiveStartKey = this.cursorCodec.decode(nextCursor);
        }

        var request = QueryEnhancedRequest.builder()
                                          .queryConditional(queryConditional)
                                          .limit(limit)
                                          .exclusiveStartKey(exclusiveStartKey)
                                          .scanIndexForward(false)
                                          .build();

        return this.smokingReports.index(SmokingReport.REPORT_DATE_EXPIRES_AT_INDEX)
                                  .query(request)
                                  .stream()
                                  .findFirst()
                                  .map(this::toPage)
                                  .orElseGet(() -> new SmokingReportPage(List.of(), null));
    }

    public @Nullable SmokingReport findByCarNumberLine(String carNumber, TrainLine line) {
        Objects.requireNonNull(carNumber);
        Objects.requireNonNull(line);

        String partitionKey = SmokingReportKeys.carNumberLine(carNumber, line);

        var key = Key.builder()
                     .partitionValue(partitionKey)
                     .build();

        return this.smokingReports.getItem(key);
    }

    private SmokingReportPage toPage(Page<SmokingReport> page) {
        List<SmokingReport> reports = List.copyOf(page.items());

        Map<String, AttributeValue> lastEvaluatedKey = page.lastEvaluatedKey();

        if (lastEvaluatedKey == null) {
            return new SmokingReportPage(reports, null);
        }

        String nextCursor;

        try {
            nextCursor = this.cursorCodec.encode(lastEvaluatedKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to encode cursor", e);
        }

        return new SmokingReportPage(reports, nextCursor);
    }
}
