package dev.aroncalvert.gtfspulse.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import dev.aroncalvert.gtfspulse.entity.Calendar;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.ArrivalDTO;
import dev.aroncalvert.gtfspulse.dto.StopDTO;
import dev.aroncalvert.gtfspulse.dto.TripUpdateData;
import dev.aroncalvert.gtfspulse.entity.Route;
import dev.aroncalvert.gtfspulse.entity.Stop;
import dev.aroncalvert.gtfspulse.entity.StopTime;
import dev.aroncalvert.gtfspulse.repository.CalendarDateRepository;
import dev.aroncalvert.gtfspulse.repository.CalendarRepository;
import dev.aroncalvert.gtfspulse.repository.RouteRepository;
import dev.aroncalvert.gtfspulse.repository.StopRepository;
import dev.aroncalvert.gtfspulse.repository.StopTimeRepository;
import dev.aroncalvert.gtfspulse.service.mapper.StopMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StopService {

  private final StopRepository stopRepository;
  private final StopTimeRepository stopTimeRepository;
  private final RouteRepository routeRepository;
  private final CalendarDateRepository calendarDateRepository;
  private final CalendarRepository calendarRepository;
  private final StopMapper stopMapper;
  private final TripUpdateService tripUpdateService;

  public StopDTO getStopInfo(String stopId) {
    Stop stop = stopRepository.findById(stopId)
        .orElseThrow(() -> new NoSuchElementException("No stop found with id: " + stopId));
    return stopMapper.toDto(stop);
  }

  // TODO add caching or fix speed otherwise (batching???)
  public List<ArrivalDTO> getArrivals(String stopId) {
    var stopTimes = getStopTimesOrderedByArrival(stopId);
    LocalDate today = LocalDate.now();
    Set<String> activeServiceIds = getActiveServiceIds(today);
    Map<String, String> shortNames = getRouteShortNames(stopTimes);

    return stopTimes.stream()
        .filter(st -> activeServiceIds.contains(st.getTrip().getServiceId()))
        .map(st -> new ArrivalDTO(
            shortNames.getOrDefault(st.getTrip().getRouteId(), st.getTrip().getRouteId()),
            st.getStopHeadsign(),
            resolveArrivalTime(st)))
        .distinct()
        .toList();
  }

  public List<StopDTO> getAllStops() {
    var stops = stopRepository.findAll();
    return stops.stream()
        .map(stopMapper::toDto)
        .toList();
  }

  private List<StopTime> getStopTimesOrderedByArrival(String stopId) {
    Stop stop = stopRepository.findById(stopId)
        .orElseThrow(() -> new NoSuchElementException("No stop found with id: " + stopId));
    return stopTimeRepository.findByStopOrderByArrivalTimeAsc(stop);
  }

  private Set<String> getActiveServiceIds(LocalDate today) {
    DayOfWeek dayOfWeek = today.getDayOfWeek();
    Set<String> activeServiceIds = calendarRepository
        .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
        .stream()
        .filter(c -> switch (dayOfWeek) {
          case MONDAY -> c.getMonday() == 1;
          case TUESDAY -> c.getTuesday() == 1;
          case WEDNESDAY -> c.getWednesday() == 1;
          case THURSDAY -> c.getThursday() == 1;
          case FRIDAY -> c.getFriday() == 1;
          case SATURDAY -> c.getSaturday() == 1;
          case SUNDAY -> c.getSunday() == 1;
        })
        .map(Calendar::getServiceId)
        .collect(Collectors.toCollection(HashSet::new));

    calendarDateRepository.findByDate(today).forEach(cd -> {
      if (cd.getExceptionType() == 1)
        activeServiceIds.add(cd.getServiceId());
      else if (cd.getExceptionType() == 2)
        activeServiceIds.remove(cd.getServiceId());
    });
    return activeServiceIds;
  }

  private Map<String, String> getRouteShortNames(List<StopTime> stopTimes) {
    Set<String> routeIds = stopTimes.stream()
        .map(st -> st.getTrip().getRouteId())
        .collect(Collectors.toSet());

    return routeRepository.findAllById(routeIds).stream()
        .collect(Collectors.toMap(Route::getRouteId, Route::getRouteShortName));
  }

  private long resolveArrivalTime(StopTime stopTime) {
    String tripId = stopTime.getTrip().getTripId();
    int stopSequence = stopTime.getStopSequence();
    int arrivalTime = stopTime.getArrivalTime();

    TripUpdateData tripUpdate = tripUpdateService.getTripUpdate(tripId);
    long scheduledTime = LocalDate.now(ZoneId.of("Europe/Dublin")).atStartOfDay(ZoneId.of("Europe/Dublin"))
        .toEpochSecond() + arrivalTime;

    if (tripUpdate == null) {
      return scheduledTime;
    }

    for (var stopTimeUpdate : tripUpdate.stopTimeUpdates()) {
      if (stopTimeUpdate.stopSequence() == stopSequence) {
        return stopTimeUpdate.arrivalTime();
      }
    }
    return scheduledTime;
  }
}
