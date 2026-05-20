package dev.aroncalvert.gtfspulse.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.transit.realtime.GtfsRealtime;

import lombok.RequiredArgsConstructor;

import dev.aroncalvert.gtfspulse.dto.BusData;

@Service
@RequiredArgsConstructor
public class BusPositionService {
  private final ConcurrentHashMap<String, BusData> map = new ConcurrentHashMap<>();
  private final KafkaTemplate<String, BusData> kafkaTemplate;

  public void updatePositions(GtfsRealtime.FeedMessage feed) {
    List<GtfsRealtime.FeedEntity> entities = feed.getEntityList();

    for (GtfsRealtime.FeedEntity entity : entities) {
      if (!entity.hasVehicle())
        continue;

      var vehiclePosition = entity.getVehicle();
      var trip = vehiclePosition.getTrip();
      var position = vehiclePosition.getPosition();
      var vehicleDescriptor = vehiclePosition.getVehicle();

      BusData busData = new BusData(
          trip.getTripId(),
          trip.getRouteId(),
          vehicleDescriptor.getId(),
          position.getLatitude(),
          position.getLongitude(),
          position.getBearing(),
          position.getSpeed(),
          vehiclePosition.getCurrentStopSequence(),
          vehiclePosition.getStopId(),
          vehiclePosition.getCurrentStatus().name(),
          vehiclePosition.getTimestamp());

      map.put(trip.getTripId(), busData);
      kafkaTemplate.send("bus-positions", trip.getTripId(), busData);
    }
  }

  public BusData getPosition(String tripId) {
    return map.get(tripId);
  }
}
