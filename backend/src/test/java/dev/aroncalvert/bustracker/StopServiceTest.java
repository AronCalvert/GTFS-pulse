package dev.aroncalvert.bustracker;

import dev.aroncalvert.gtfspulse.dto.ArrivalDTO;
import dev.aroncalvert.gtfspulse.entity.*;
import dev.aroncalvert.gtfspulse.repository.*;
import dev.aroncalvert.gtfspulse.service.StopService;
import dev.aroncalvert.gtfspulse.service.TripUpdateService;
import dev.aroncalvert.gtfspulse.service.mapper.StopMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StopServiceTest {

  @Mock
  CalendarRepository calendarRepository;

  @Mock
  StopRepository stopRepository;

  @Mock
  StopTimeRepository stopTimeRepository;

  @Mock
  CalendarDateRepository calendarDateRepository;

  @Mock
  RouteRepository routeRepository;

  @Mock
  TripUpdateService tripUpdateService;

  @Mock
  StopMapper stopMapper;

  @InjectMocks
  StopService stopService;

  @Test
  void getArrivals_withActiveServiceId_returnsArrival() {
    String stopId = "stop-1";
    LocalDate today = LocalDate.now();

    Stop stop = new Stop();
    stop.setStopId(stopId);

    Trip trip = new Trip();
    trip.setTripId("trip-1");
    trip.setServiceId("service-1");
    trip.setRouteId("route-1");

    StopTime stopTime = new StopTime();
    stopTime.setTrip(trip);
    stopTime.setStopHeadsign("City Centre");
    stopTime.setArrivalTime(3600);
    stopTime.setStopSequence(1);

    Calendar calendar = new Calendar();
    calendar.setServiceId("service-1");
    calendar.setMonday(1);
    calendar.setTuesday(1);
    calendar.setWednesday(1);
    calendar.setThursday(1);
    calendar.setFriday(1);
    calendar.setSaturday(1);
    calendar.setSunday(1);

    Route route = new Route();
    route.setRouteId("route-1");
    route.setRouteShortName("39A");

    when(stopRepository.findById(stopId)).thenReturn(Optional.of(stop));
    when(stopTimeRepository.findByStopOrderByArrivalTimeAsc(stop)).thenReturn(List.of(stopTime));
    when(calendarRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today))
        .thenReturn(List.of(calendar));
    when(calendarDateRepository.findByDate(today)).thenReturn(List.of());
    when(routeRepository.findAllById(anyIterable())).thenReturn(List.of(route));
    when(tripUpdateService.getTripUpdate("trip-1")).thenReturn(null);

    List<ArrivalDTO> result = stopService.getArrivals(stopId);

    assertEquals(1, result.size());
    assertEquals("39A", result.get(0).routeShortName());
    assertEquals("City Centre", result.get(0).headsign());
  }
}
