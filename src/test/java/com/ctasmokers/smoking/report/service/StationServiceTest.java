package com.ctasmokers.smoking.report.service;

import com.cta4j.train.station.StationsApi;
import com.cta4j.train.station.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {
    @Mock
    private StationsApi stationsApi;

    private static Station station(String mapId) {
        Station station = mock(Station.class);

        when(station.mapId()).thenReturn(mapId);

        return station;
    }

    @Test
    void getIds_returnsDistinctMapIds() {
        List<Station> stations = List.of(station("40900"), station("41220"), station("40900"));

        when(stationsApi.list()).thenReturn(stations);

        Set<String> ids = new StationService(stationsApi).getIds();

        assertThat(ids).containsExactlyInAnyOrder("40900", "41220");
    }

    @Test
    void getIds_returnsUnmodifiableSet() {
        List<Station> stations = List.of(station("40900"));

        when(stationsApi.list()).thenReturn(stations);

        Set<String> ids = new StationService(stationsApi).getIds();

        assertThatThrownBy(() -> ids.add("1")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getIds_noStations_returnsEmptySet() {
        when(stationsApi.list()).thenReturn(List.of());

        assertThat(new StationService(stationsApi).getIds()).isEmpty();
    }
}
