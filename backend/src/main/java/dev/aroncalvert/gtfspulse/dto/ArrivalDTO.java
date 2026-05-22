package dev.aroncalvert.gtfspulse.dto;

public record ArrivalDTO(
    String routeShortName,
    String headsign,
    int arrivalTime) {
}
