package dev.aroncalvert.gtfspulse.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;

import dev.aroncalvert.gtfspulse.dto.BusData;
import dev.aroncalvert.gtfspulse.dto.StopTimeDTO;
import dev.aroncalvert.gtfspulse.service.BusPositionService;
import dev.aroncalvert.gtfspulse.service.TripService;

@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusController {

  private final BusPositionService busPositionService;
  private final TripService tripService;

  @PostMapping("/update")
  public ResponseEntity<String> updateBusPositions(@RequestBody byte[] payload) throws InvalidProtocolBufferException {
    var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
    busPositionService.updatePositions(feed);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{trip_id}")
  public ResponseEntity<BusData> getBusPosition(@PathVariable("trip_id") String tripId) {
    return ResponseEntity.ok(busPositionService.getPosition(tripId));
  }

  @GetMapping("/{trip_id}/stops")
  public ResponseEntity<List<StopTimeDTO>> getStopsInOrder(@PathVariable("trip_id") String tripId) {
    return ResponseEntity.ok(tripService.getStopsInOrder(tripId));
  }

  @GetMapping("/{trip_id}/stops/passed")
  public ResponseEntity<List<StopTimeDTO>> getStopsPassed(@PathVariable("trip_id") String tripId) {
    BusData position = busPositionService.getPosition(tripId);
    int stopSequence = position.currentStopSequence();
    return ResponseEntity.ok(tripService.getStopsPassed(tripId, stopSequence));
  }

  @GetMapping("/{trip_id}/stops/next")
  public ResponseEntity<StopTimeDTO> getNextStop(@PathVariable("trip_id") String tripId) {
    BusData position = busPositionService.getPosition(tripId);
    int stopSequence = position.currentStopSequence();
    return ResponseEntity.ok(tripService.getNextStop(tripId, stopSequence));
  }
}
