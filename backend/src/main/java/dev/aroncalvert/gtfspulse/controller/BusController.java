package dev.aroncalvert.gtfspulse.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;

import dev.aroncalvert.gtfspulse.service.BusPositionService;

@RestController
@RequestMapping("/api/v1/bus")
@RequiredArgsConstructor
public class BusController {

  private final BusPositionService busPositionService;

  @PostMapping("/update")
  public ResponseEntity<String> updateBusPositions(@RequestBody byte[] payload) throws InvalidProtocolBufferException {
    var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
    busPositionService.updatePositions(feed);
    return ResponseEntity.ok().build();
  }
}
