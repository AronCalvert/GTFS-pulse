package dev.aroncalvert.gtfspulse.dto;

public record StopTimeDTO(
    String tripId,
    int arrivalTime,
    int departureTime,
    String stopId,
    int stopSequence,
    String stopHeadsign,
    int pickupType,
    int dropOffType,
    int timepoint) {
}
