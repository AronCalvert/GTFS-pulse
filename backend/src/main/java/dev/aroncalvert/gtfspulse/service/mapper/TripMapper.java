package dev.aroncalvert.gtfspulse.service.mapper;

import org.springframework.stereotype.Component;

import dev.aroncalvert.gtfspulse.dto.TripDTO;
import dev.aroncalvert.gtfspulse.entity.Trip;

@Component
public class TripMapper {

  public TripDTO toDto(Trip trip) {
    return new TripDTO(
        trip.getTripId(),
        trip.getRouteId(),
        trip.getTripHeadSign(),
        trip.getDirectionId());
  }
}
