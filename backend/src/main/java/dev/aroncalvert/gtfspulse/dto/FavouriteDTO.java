package dev.aroncalvert.gtfspulse.dto;

public record FavouriteDTO(
    long Id,
    long userId,
    String stopId,
    String routeId) {
}
