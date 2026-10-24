package com.ctasmokers.smoking.report.service;

import com.cta4j.train.common.exception.Cta4jTrainException;
import com.cta4j.train.station.StationsApi;
import com.cta4j.train.station.model.Station;
import com.ctasmokers.common.config.CacheConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(StationServiceCachingTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StationServiceCachingTest {
    @Configuration
    @Import({CacheConfig.class, StationService.class})
    static class TestConfig {
        @Bean
        StationsApi stationsApi() {
            return Mockito.mock(StationsApi.class);
        }
    }

    @Autowired
    private StationService stationService;

    @Autowired
    private StationsApi stationsApi;

    private static Station station(String mapId) {
        Station station = mock(Station.class);

        when(station.mapId()).thenReturn(mapId);

        return station;
    }

    @Test
    void getIds_calledTwice_hitsApiOnce() {
        reset(stationsApi);
        List<Station> stations = List.of(station("40900"));
        when(stationsApi.list()).thenReturn(stations);

        Set<String> first = stationService.getIds();
        Set<String> second = stationService.getIds();

        assertThat(first).containsExactly("40900");
        assertThat(second).isSameAs(first);
        verify(stationsApi, times(1)).list();
    }

    @Test
    void getIds_transientFailure_isRetried() {
        reset(stationsApi);
        List<Station> stations = List.of(station("40900"));
        when(stationsApi.list())
            .thenThrow(new Cta4jTrainException("boom", "transient"))
            .thenReturn(stations);

        Set<String> ids = stationService.getIds();

        assertThat(ids).containsExactly("40900");
        verify(stationsApi, times(2)).list();
    }

    @Test
    void getIds_nonRetryableFailure_isNotRetried() {
        reset(stationsApi);
        when(stationsApi.list()).thenThrow(new IllegalStateException("fatal"));

        assertThatThrownBy(() -> stationService.getIds()).isInstanceOf(IllegalStateException.class);

        verify(stationsApi, times(1)).list();
    }
}
