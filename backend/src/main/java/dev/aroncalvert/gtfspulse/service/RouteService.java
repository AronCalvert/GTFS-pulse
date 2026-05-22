package dev.aroncalvert.gtfspulse.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.RouteDTO;
import dev.aroncalvert.gtfspulse.dto.TripDTO;
import dev.aroncalvert.gtfspulse.repository.RouteRepository;
import dev.aroncalvert.gtfspulse.repository.TripRepository;
import dev.aroncalvert.gtfspulse.service.mapper.RouteMapper;
import dev.aroncalvert.gtfspulse.service.mapper.TripMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteService {

  private final RouteRepository routeRepository;
  private final TripRepository tripRepository;
  private final RouteMapper routeMapper;
  private final TripMapper tripMapper;

  public RouteDTO getRouteById(String routeId) {
    return routeRepository.findById(routeId)
        .map(routeMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("No route found with id: " + routeId));
  }

  public List<TripDTO> getTripsForRoute(String routeId) {
    return tripRepository.findByRouteId(routeId).stream()
        .map(tripMapper::toDto)
        .toList();
  }
}
