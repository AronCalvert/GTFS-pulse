package dev.aroncalvert.gtfspulse.dto;

public record VehicleData(
    String tripId,
    String routeId,
    String vehicleId,
    double latitude,
    double longitude,
    double bearing,
    float speed,
    int currentStopSequence,
    String stopId,
    String currentStatus,
    long timestamp) {
}
