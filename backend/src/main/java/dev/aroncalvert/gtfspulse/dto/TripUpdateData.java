package dev.aroncalvert.gtfspulse.dto;

import java.util.List;

public record TripUpdateData(
    String tripId,
    String routeId,
    String startTime,
    String startDate,
    List<StopTimeUpdateData> stopTimeUpdates) {

  public record StopTimeUpdateData(
      int stopSequence,
      String stopId,
      long arrivalTime,
      long departureTime) {
  }
}
