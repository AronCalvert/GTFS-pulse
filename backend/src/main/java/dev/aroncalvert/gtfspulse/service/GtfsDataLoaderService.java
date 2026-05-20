package dev.aroncalvert.gtfspulse.service;

import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import dev.aroncalvert.gtfspulse.entity.Agency;
import dev.aroncalvert.gtfspulse.entity.Route;
import dev.aroncalvert.gtfspulse.entity.Stop;
import dev.aroncalvert.gtfspulse.entity.StopTime;
import dev.aroncalvert.gtfspulse.entity.Trip;
import dev.aroncalvert.gtfspulse.entity.Calendar;
import dev.aroncalvert.gtfspulse.entity.CalendarDate;
import dev.aroncalvert.gtfspulse.repository.AgencyRepository;
import dev.aroncalvert.gtfspulse.repository.CalendarDateRepository;
import dev.aroncalvert.gtfspulse.repository.CalendarRepository;
import dev.aroncalvert.gtfspulse.repository.RouteRepository;
import dev.aroncalvert.gtfspulse.repository.StopRepository;
import dev.aroncalvert.gtfspulse.repository.StopTimeRepository;
import dev.aroncalvert.gtfspulse.repository.TripRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GtfsDataLoaderService {

  private final AgencyRepository agencyRepository;
  private final CalendarDateRepository calendarDateRepository;
  private final CalendarRepository calendarRepository;
  private final RouteRepository routeRepository;
  private final StopRepository stopRepository;
  private final StopTimeRepository stopTimeRepository;
  private final TripRepository tripRepository;
  private Path gtfsPath = Paths.get("../data");

  @PersistenceContext
  private EntityManager entityManager;

  public void loadGtfsData() throws Exception {
    System.out.println("--- GTFS Data Loader starting ---");
    loadAgency();
    System.out.println("agency loaded");
    loadRoutes();
    System.out.println("Routes loaded");
    loadCalendar();
    System.out.println("Calendar loaded");
    loadCalendarDates();
    System.out.println("Calendar dates loaded");
    List<Stop> stops = loadStops();
    System.out.println("Stops loaded");
    List<Trip> trips = loadTrips();
    System.out.println("Trips loaded");

    Map<String, Trip> tripMap = trips.stream().collect(Collectors.toMap(Trip::getTripId, t -> t));
    Map<String, Stop> stopMap = stops.stream().collect(Collectors.toMap(Stop::getStopId, s -> s));
    loadStopTimes(tripMap, stopMap);
    System.out.println("stop times loaded");
    System.out.println("All data loaded :)))");
  }

  public void loadAgency() throws IOException, CsvValidationException {
    CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("agency.txt").toFile()));
    Map<String, String> row;
    List<Agency> agencies = new ArrayList<>();
    row = reader.readMap();
    while (row != null) {
      var agency = new Agency();
      agency.setAgencyId(row.get("agency_id"));
      agency.setAgencyName(row.get("agency_name"));
      agency.setAgencyUrl(row.get("agency_url"));
      agency.setAgencyTimezone(row.get("agency_timezone"));
      agencies.add(agency);
      row = reader.readMap();
    }
    agencyRepository.saveAll(agencies);
    reader.close();
  }

  public void loadRoutes() throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("routes.txt").toFile()));
    Map<String, String> row;
    List<Route> routes = new ArrayList<Route>();
    row = reader.readMap();
    while (row != null) {
      var route = new Route();
      route.setRouteId(row.get("route_id"));
      route.setAgencyId(row.get("agency_id"));
      route.setRouteLongName(row.get("route_long_name"));
      route.setRouteShortName(row.get("route_short_name"));
      route.setRouteType(Integer.parseInt(row.get("route_type")));
      routes.add(route);
      row = reader.readMap();
    }
    routeRepository.saveAll(routes);
    reader.close();
  }

  public List<Stop> loadStops() throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("stops.txt").toFile()));
    Map<String, String> row;
    List<Stop> stops = new ArrayList<Stop>();
    row = reader.readMap();
    while (row != null) {
      var stop = new Stop();
      stop.setStopId(row.get("stop_id"));
      stop.setStopCode(row.get("stop_code"));
      stop.setStopName(row.get("stop_name"));
      stop.setStopLat(Double.parseDouble(row.get("stop_lat")));
      stop.setStopLon(Double.parseDouble(row.get("stop_lon")));
      stops.add(stop);
      row = reader.readMap();
    }
    stopRepository.saveAll(stops);
    reader.close();
    return stops;
  }

  public void loadCalendar() throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("calendar.txt").toFile()));
    Map<String, String> row;
    List<Calendar> calendars = new ArrayList<Calendar>();
    row = reader.readMap();
    while (row != null) {
      var calendar = new Calendar();
      calendar.setServiceId(row.get("service_id"));
      calendar.setMonday(Integer.parseInt(row.get("monday")));
      calendar.setTuesday(Integer.parseInt(row.get("tuesday")));
      calendar.setWednesday(Integer.parseInt(row.get("wednesday")));
      calendar.setThursday(Integer.parseInt(row.get("thursday")));
      calendar.setFriday(Integer.parseInt(row.get("friday")));
      calendar.setSaturday(Integer.parseInt(row.get("saturday")));
      calendar.setSunday(Integer.parseInt(row.get("sunday")));
      calendar.setStartDate(LocalDate.parse(row.get("start_date"), DateTimeFormatter.BASIC_ISO_DATE));
      calendar.setEndDate(LocalDate.parse(row.get("end_date"), DateTimeFormatter.BASIC_ISO_DATE));
      calendars.add(calendar);
      row = reader.readMap();
    }
    calendarRepository.saveAll(calendars);
    reader.close();
  }

  public void loadCalendarDates() throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("calendar_dates.txt").toFile()));
    Map<String, String> row;
    List<CalendarDate> calendarDates = new ArrayList<CalendarDate>();

    row = reader.readMap();
    while (row != null) {
      var calendarDate = new CalendarDate();
      calendarDate.setServiceId(row.get("service_id"));
      calendarDate.setDate(LocalDate.parse(row.get("date"), DateTimeFormatter.BASIC_ISO_DATE));
      calendarDate.setExceptionType(Integer.parseInt(row.get("exception_type")));
      calendarDates.add(calendarDate);
      row = reader.readMap();
    }
    calendarDateRepository.saveAll(calendarDates);
    reader.close();
  }

  public List<Trip> loadTrips() throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("trips.txt").toFile()));
    Map<String, String> row;
    List<Trip> trips = new ArrayList<Trip>();

    row = reader.readMap();
    while (row != null) {
      var trip = new Trip();
      trip.setServiceId(row.get("service_id"));
      trip.setRouteId(row.get("route_id"));
      trip.setBlockId(row.get("block_id"));
      trip.setDirectionId(Integer.parseInt(row.get("direction_id")));
      trip.setShapeId(row.get("shape_id"));
      trip.setTripHeadSign(row.get("trip_head_sign"));
      trip.setTripShortName(row.get("trip_short_name"));
      trip.setTripId(row.get("trip_id"));
      trips.add(trip);
      row = reader.readMap();
    }

    tripRepository.saveAll(trips);
    reader.close();
    return trips;
  }

  public void loadStopTimes(Map<String, Trip> tripMap, Map<String, Stop> stopMap)
      throws IOException, CsvValidationException {
    var reader = new CSVReaderHeaderAware(new FileReader(gtfsPath.resolve("stop_times.txt").toFile()));
    Map<String, String> row;
    List<StopTime> stopTimes = new ArrayList<StopTime>();
    int count = 0;

    row = reader.readMap();
    while (row != null) {
      var stopTime = new StopTime();
      Trip trip = tripMap.get(row.get("trip_id"));
      Stop stop = stopMap.get(row.get("stop_id"));

      if (trip == null || stop == null) {
        row = reader.readMap();
        System.out.println("Skipping row - null trip or stop: " + row.get("trip_id") + " " + row.get("stop_id"));
        continue;
      }

      stopTime.setTrip(trip);
      stopTime.setStop(stop);
      stopTime.setArrivalTime(parseGtfsTime(row.get("arrival_time")));
      stopTime.setDepartureTime(parseGtfsTime(row.get("departure_time")));
      stopTime.setStopHeadsign(row.get("stop_headsign"));
      stopTime.setDropOffType(Integer.parseInt(row.get("drop_off_type")));
      stopTime.setPickupType(Integer.parseInt(row.get("pickup_type")));
      stopTime.setStopSequence(Integer.parseInt(row.get("stop_sequence")));
      stopTime.setTimepoint(Integer.parseInt(row.get("timepoint")));
      stopTimes.add(stopTime);
      count++;
      row = reader.readMap();

      if (stopTimes.size() >= 10000) {
        System.out.println("trying to save 10000");
        try {
          stopTimeRepository.saveAll(stopTimes);
        } catch (Exception e) {
          System.out.println("Save failed: " + e.getMessage());
          e.printStackTrace();
          stopTimes.clear();
        }
        System.out.println("10000 stop times saved");
        System.out.println(count + " stop times processes so far");
        stopTimes.clear();
      }
    }
    stopTimeRepository.saveAll(stopTimes);
    System.out.println("all saved :>");
    reader.close();
  }

  private int parseGtfsTime(String time) {
    String[] parts = time.split(":");
    return Integer.parseInt(parts[0]) * 3600
        + Integer.parseInt(parts[1]) * 60
        + Integer.parseInt(parts[2]);
  }
}
