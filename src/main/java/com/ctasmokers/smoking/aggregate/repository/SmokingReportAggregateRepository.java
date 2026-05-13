package com.ctasmokers.smoking.aggregate.repository;

import com.ctasmokers.aws.config.DynamoDbTableProperties;
import com.ctasmokers.smoking.aggregate.model.SmokingReportAggregate;
import com.ctasmokers.smoking.common.model.TrainLine;
import com.ctasmokers.smoking.common.model.YearWeek;
import org.jspecify.annotations.NullMarked;
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
import java.util.Optional;

@Repository
@NullMarked
public final class SmokingReportAggregateRepository {
    private static final String PK_TEMPLATE = "LINE#%s";
    private static final String SK_DAY_TEMPLATE = "DAY#%s";
    private static final String SK_WEEK_TEMPLATE = "WEEK#%s";
    private static final String SK_MONTH_TEMPLATE = "MONTH#%s";
    private static final String SK_YEAR_TEMPLATE = "YEAR#%s";
    private static final String SK_ALL_TIME = "ALL_TIME";

    private final DynamoDbTable<SmokingReportAggregate> smokingReportAggregates;

    @Autowired
    public SmokingReportAggregateRepository(
        DynamoDbEnhancedClient dynamoDbClient,
        DynamoDbTableProperties tableProperties
    ) {
        TableSchema<SmokingReportAggregate> tableSchema = TableSchema.fromImmutableClass(SmokingReportAggregate.class);

        this.smokingReportAggregates = dynamoDbClient.table(tableProperties.smokingReportAggregates(), tableSchema);
    }

    public Optional<SmokingReportAggregate> findByLineAndDay(TrainLine line, LocalDate day) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(day);

        String pk = PK_TEMPLATE.formatted(line);
        String sk = SK_DAY_TEMPLATE.formatted(day);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        SmokingReportAggregate aggregate = this.smokingReportAggregates.getItem(key);

        return Optional.ofNullable(aggregate);
    }

    public Optional<SmokingReportAggregate> findByLineAndWeek(TrainLine line, YearWeek yearWeek) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearWeek);

        String pk = PK_TEMPLATE.formatted(line);
        String sk = SK_WEEK_TEMPLATE.formatted(yearWeek);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        SmokingReportAggregate aggregate = this.smokingReportAggregates.getItem(key);

        return Optional.ofNullable(aggregate);
    }

    public Optional<SmokingReportAggregate> findByLineAndMonth(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        String pk = PK_TEMPLATE.formatted(line);
        String sk = SK_MONTH_TEMPLATE.formatted(yearMonth);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        SmokingReportAggregate aggregate = this.smokingReportAggregates.getItem(key);

        return Optional.ofNullable(aggregate);
    }

    public Optional<SmokingReportAggregate> findByLineAndYear(TrainLine line, Year year) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(year);

        String pk = PK_TEMPLATE.formatted(line);
        String sk = SK_YEAR_TEMPLATE.formatted(year);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(sk)
                     .build();

        SmokingReportAggregate aggregate = this.smokingReportAggregates.getItem(key);

        return Optional.ofNullable(aggregate);
    }

    public Optional<SmokingReportAggregate> findByLineAllTime(TrainLine line) {
        Objects.requireNonNull(line);

        String pk = PK_TEMPLATE.formatted(line);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(SK_ALL_TIME)
                     .build();

        SmokingReportAggregate aggregate = this.smokingReportAggregates.getItem(key);

        return Optional.ofNullable(aggregate);
    }

    public List<SmokingReportAggregate> findDaysByLineAndMonth(TrainLine line, YearMonth yearMonth) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(yearMonth);

        String pk = PK_TEMPLATE.formatted(line);
        String skPrefix = SK_DAY_TEMPLATE.formatted(yearMonth);

        Key key = Key.builder()
                     .partitionValue(pk)
                     .sortValue(skPrefix)
                     .build();

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(key);

        return this.smokingReportAggregates.query(queryConditional)
                                           .items()
                                           .stream()
                                           .toList();
    }
}
