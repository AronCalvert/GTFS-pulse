package dev.aroncalvert.gtfs_ingestor;

import java.util.List;

public record TripUpdateData(
    String tripId,
    String routeId,
    String startTime,
    String startDate,
    List<StopTimeUpdateData> stopTimeUpdates) {

  record StopTimeUpdateData(
      int stopSequence,
      String stopId,
      long arrivalTime,
      long departureTime) {
  }
}
