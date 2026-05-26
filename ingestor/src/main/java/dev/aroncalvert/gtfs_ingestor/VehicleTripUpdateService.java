package dev.aroncalvert.gtfs_ingestor;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.transit.realtime.GtfsRealtime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleTripUpdateService {
  private final KafkaTemplate<String, TripUpdateData> kafkaTemplate;

  public void updateUpdates(GtfsRealtime.FeedMessage feed) {
    List<GtfsRealtime.FeedEntity> entities = feed.getEntityList();

    for (GtfsRealtime.FeedEntity entity : entities) {
      if (!entity.hasTripUpdate())
        continue;

      GtfsRealtime.TripUpdate tripUpdate = entity.getTripUpdate();
      GtfsRealtime.TripDescriptor trip = tripUpdate.getTrip();
      List<GtfsRealtime.TripUpdate.StopTimeUpdate> stopTimes = tripUpdate.getStopTimeUpdateList();

      List<TripUpdateData.StopTimeUpdateData> updates = stopTimes.stream()
          .map(stu -> new TripUpdateData.StopTimeUpdateData(
              stu.getStopSequence(),
              stu.getStopId(),
              stu.getArrival().getTime(),
              stu.getDeparture().getTime()))
          .toList();

      TripUpdateData tripUpdateData = new TripUpdateData(
          trip.getTripId(),
          trip.getRouteId(),
          trip.getStartTime(),
          trip.getStartDate(),
          updates);
      kafkaTemplate.send("trip-updates", trip.getTripId(), tripUpdateData);
    }
  }
}
