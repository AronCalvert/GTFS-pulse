package dev.aroncalvert.gtfspulse.service.mapper;

import org.springframework.stereotype.Component;

import dev.aroncalvert.gtfspulse.dto.RouteDTO;
import dev.aroncalvert.gtfspulse.entity.Route;

@Component
public class RouteMapper {

  public RouteDTO toDto(Route route) {
    return new RouteDTO(
        route.getRouteId(),
        route.getAgencyId(),
        route.getRouteShortName(),
        route.getRouteLongName(),
        route.getRouteType());
  }
}
