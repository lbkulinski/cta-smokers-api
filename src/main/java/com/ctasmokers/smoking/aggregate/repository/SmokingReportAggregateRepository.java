package com.ctasmokers.smoking.aggregate.repository;

import com.cta4j.train.common.model.TrainLine;
import com.ctasmokers.aws.config.DynamoDbProperties;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.common.model.YearWeek;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Repository
@NullMarked
public final class SmokingReportAggregateRepository {
    private static final String PK_TEMPLATE = "LINE#%s";
    private static final String SK_DAY_PREFIX = "DAY#";
    private static final String SK_DAY_TEMPLATE = SK_DAY_PREFIX + "%s";
    private static final String SK_WEEK_TEMPLATE = "WEEK#%s";
    private static final String SK_MONTH_TEMPLATE = "MONTH#%s";
    private static final String SK_YEAR_TEMPLATE = "YEAR#%s";
    private static final String SK_ALL_TIME = "ALL_TIME";

    private final DynamoDbTable<@Nullable SmokingReportAggregate> smokingReportAggregates;

    @Autowired
    public SmokingReportAggregateRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        DynamoDbProperties tableProperties
    ) {
        String tableName = tableProperties.smokingReportAggregates();
        var tableSchema = TableSchema.fromImmutableClass(SmokingReportAggregate.class);

        this.smokingReportAggregates = dynamoDbClient.table(tableName, tableSchema);
    }

    public @Nullable SmokingReportAggregate findByLineAndDay(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        var pk = PK_TEMPLATE.formatted(line);
        var sk = SK_DAY_TEMPLATE.formatted(day);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        return this.smokingReportAggregates.getItem(key);
    }

    public @Nullable SmokingReportAggregate findByLineAndWeek(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        var pk = PK_TEMPLATE.formatted(line);
        var sk = SK_WEEK_TEMPLATE.formatted(yearWeek);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        return this.smokingReportAggregates.getItem(key);
    }

    public @Nullable SmokingReportAggregate findByLineAndMonth(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        var pk = PK_TEMPLATE.formatted(line);
        var sk = SK_MONTH_TEMPLATE.formatted(yearMonth);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        return this.smokingReportAggregates.getItem(key);
    }

    public @Nullable SmokingReportAggregate findByLineAndYear(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        var pk = PK_TEMPLATE.formatted(line);
        var sk = SK_YEAR_TEMPLATE.formatted(year);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        return this.smokingReportAggregates.getItem(key);
    }

    public @Nullable SmokingReportAggregate findByLineAllTime(TrainLine line) {
        Objects.requireNonNull(line);

        var pk = PK_TEMPLATE.formatted(line);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(SK_ALL_TIME)
                     .build();

        return this.smokingReportAggregates.getItem(key);
    }

    public List<SmokingReportAggregate> findDayAggregatesByLineAndMonth(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        var pk = PK_TEMPLATE.formatted(line);
        var skPrefix = SK_DAY_TEMPLATE.formatted(yearMonth);

        var key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(skPrefix)
                     .build();

        var queryConditional = QueryConditional.sortBeginsWith(key);

        return this.smokingReportAggregates.query(queryConditional)
                                           .items()
                                           .stream()
                                           .filter(Objects::nonNull)
                                           .toList();
    }
}
