package dev.aroncalvert.gtfspulse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aroncalvert.gtfspulse.dto.ArrivalDTO;
import dev.aroncalvert.gtfspulse.dto.StopDTO;
import dev.aroncalvert.gtfspulse.service.StopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
public class StopController {

  private final StopService stopService;

  @GetMapping("/{stop_id}")
  public ResponseEntity<StopDTO> getStopInfo(@PathVariable("stop_id") String stopId) {
    return ResponseEntity.ok(stopService.getStopInfo(stopId));
  }

  @GetMapping("/{stop_id}/arrivals")
  public ResponseEntity<ArrivalDTO> getUpcomingStopArrivals(@PathVariable("stop_id") String stopId) {

  }
}
