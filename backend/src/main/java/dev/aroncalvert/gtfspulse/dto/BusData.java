package dev.aroncalvert.gtfspulse.dto;

public record BusData(
    String tripId,
    String routeId,
    String vehicleId,
    double latitude,
    double longitude,
    double bearing) {
}
