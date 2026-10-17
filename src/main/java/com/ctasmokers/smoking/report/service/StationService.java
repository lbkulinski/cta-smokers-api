package com.ctasmokers.smoking.report.service;

import com.cta4j.train.common.exception.Cta4jTrainException;
import com.cta4j.train.station.StationsApi;
import com.cta4j.train.station.model.Station;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@NullMarked
public class StationService {
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final StationsApi stationsApi;

    public StationService(StationsApi stationsApi) {
        this.stationsApi = stationsApi;
    }

    @Cacheable(value = "stationIds", sync = true)
    @Retryable(includes = Cta4jTrainException.class, multiplier = BACKOFF_MULTIPLIER)
    public Set<String> getIds() {
        return this.stationsApi.list()
                               .stream()
                               .map(Station::mapId)
                               .collect(Collectors.toUnmodifiableSet());
    }
}
