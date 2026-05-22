package dev.aroncalvert.gtfspulse.dto;

public record RouteDTO(
    String routeId,
    String agencyId,
    String routeShortName,
    String routeLongName,
    int routeType) {
}
