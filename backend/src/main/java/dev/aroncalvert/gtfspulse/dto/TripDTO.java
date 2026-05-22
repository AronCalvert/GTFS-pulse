package dev.aroncalvert.gtfspulse.dto;

public record TripDTO(
    String tripId,
    String routeId,
    String headsign,
    int directionId) {
}
