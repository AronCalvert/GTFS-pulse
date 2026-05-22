package dev.aroncalvert.gtfs_ingestor;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.transit.realtime.GtfsRealtime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehiclePositionService {
  private final KafkaTemplate<String, VehicleData> kafkaTemplate;

  public void updatePositions(GtfsRealtime.FeedMessage feed) {
    List<GtfsRealtime.FeedEntity> entities = feed.getEntityList();

    for (GtfsRealtime.FeedEntity entity : entities) {
      if (!entity.hasVehicle())
        continue;

      var vehiclePosition = entity.getVehicle();
      var trip = vehiclePosition.getTrip();
      var position = vehiclePosition.getPosition();
      var vehicleDescriptor = vehiclePosition.getVehicle();

      VehicleData vehicleData = new VehicleData(
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

      kafkaTemplate.send("vehicle-positions", trip.getTripId(), vehicleData);
    }
  }
}
