package dev.aroncalvert.gtfspulse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aroncalvert.gtfspulse.service.GtfsDataLoaderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final GtfsDataLoaderService gtfsDataLoaderService;

  @PostMapping("/reload-gtfs")
  public ResponseEntity<String> reloadGtfs() throws Exception {
    gtfsDataLoaderService.loadGtfsData();
    return ResponseEntity.ok("GTFS data reload triggered successfully");
  }
}
