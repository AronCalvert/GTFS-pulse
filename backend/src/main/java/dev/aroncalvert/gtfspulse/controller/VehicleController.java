package dev.aroncalvert.gtfspulse.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.aroncalvert.gtfspulse.dto.VehicleData;
import dev.aroncalvert.gtfspulse.dto.StopTimeDTO;
import dev.aroncalvert.gtfspulse.service.VehiclePositionService;
import dev.aroncalvert.gtfspulse.service.TripService;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleController {

  private final VehiclePositionService vehiclePositionService;
  private final TripService tripService;

  @GetMapping("/{trip_id}")
  public ResponseEntity<VehicleData> getVehiclePosition(@PathVariable("trip_id") String tripId) {
    return ResponseEntity.ok(vehiclePositionService.getPosition(tripId));
  }

  @GetMapping("/{trip_id}/stops")
  public ResponseEntity<List<StopTimeDTO>> getStopsInOrder(@PathVariable("trip_id") String tripId) {
    return ResponseEntity.ok(tripService.getStopsInOrder(tripId));
  }

  @GetMapping("/{trip_id}/stops/passed")
  public ResponseEntity<List<StopTimeDTO>> getStopsPassed(@PathVariable("trip_id") String tripId) {
    VehicleData position = vehiclePositionService.getPosition(tripId);
    int stopSequence = position.currentStopSequence();
    return ResponseEntity.ok(tripService.getStopsPassed(tripId, stopSequence));
  }

  @GetMapping("/{trip_id}/stops/next")
  public ResponseEntity<StopTimeDTO> getNextStop(@PathVariable("trip_id") String tripId) {
    VehicleData position = vehiclePositionService.getPosition(tripId);
    int stopSequence = position.currentStopSequence();
    return ResponseEntity.ok(tripService.getNextStop(tripId, stopSequence));
  }
}
