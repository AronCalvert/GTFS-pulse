package dev.aroncalvert.gtfspulse.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aroncalvert.gtfspulse.dto.RouteDTO;
import dev.aroncalvert.gtfspulse.dto.TripDTO;
import dev.aroncalvert.gtfspulse.service.RouteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

  private final RouteService routeService;

  @GetMapping("/{route_id}")
  public ResponseEntity<RouteDTO> getRoute(@PathVariable("route_id") String routeId) {
    return ResponseEntity.ok(routeService.getRouteById(routeId));
  }

  @GetMapping("/{route_id}/trips")
  public ResponseEntity<List<TripDTO>> getTripsForRoute(@PathVariable("route_id") String routeId) {
    return ResponseEntity.ok(routeService.getTripsForRoute(routeId));
  }
}
