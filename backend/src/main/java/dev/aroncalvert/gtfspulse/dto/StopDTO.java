package dev.aroncalvert.gtfspulse.dto;

public record StopDTO(
    String stopId,
    String stopCode,
    String stopName,
    double stopLat,
    double stopLon) {
}
