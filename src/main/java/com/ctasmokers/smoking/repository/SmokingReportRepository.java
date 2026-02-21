package com.ctasmokers.smoking.repository;

import com.ctasmokers.smoking.model.SmokingReport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@NullMarked
public final class SmokingReportRepository {
    public static final String DATE_KEY = "date";
    public static final String REPORT_ID_KEY = "reportId";

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

    public record SmokingReportPage(
        List<SmokingReport> reports,
        @Nullable Map<String, AttributeValue> lastEvaluatedKey
    ) {
        public SmokingReportPage {
            Objects.requireNonNull(reports);

            reports.forEach(Objects::requireNonNull);

            reports = List.copyOf(reports);
        }
    }

    public SmokingReportPage findPageByDate(
        LocalDate date,
        int pageSize,
        @Nullable String nextCursor
    ) {
        Objects.requireNonNull(date);

        String dateString = date.toString();

        Key key = Key.builder()
                     .partitionValue(dateString)
                     .build();

        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);

        Map<String, AttributeValue> exclusiveStartKey = null;

        if (nextCursor != null) {
            exclusiveStartKey = Map.of(
                DATE_KEY, AttributeValue.builder()
                                      .s(dateString)
                                      .build(),
                REPORT_ID_KEY, AttributeValue.builder()
                                          .s(nextCursor)
                                          .build()
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
                                      page.lastEvaluatedKey()
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
}
