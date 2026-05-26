package dev.aroncalvert.gtfs_ingestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.google.transit.realtime.GtfsRealtime;

@Service
public class GtfsFeedPoller {
  private static final Logger log = LoggerFactory.getLogger(GtfsFeedPoller.class);
  private final RestClient restClient;
  private final VehiclePositionService vehiclePositionService;
  private final VehicleTripUpdateService vehicleTripUpdateService;

  public GtfsFeedPoller(RestClient.Builder restClientBuilder, VehiclePositionService vehiclePositionService,
      VehicleTripUpdateService vehicleTripUpdateService,
      @Value("${gtfs.simulator.url:http://localhost:5000}") String simulatorUrl) {
    this.restClient = restClientBuilder.baseUrl(simulatorUrl).build();
    this.vehiclePositionService = vehiclePositionService;
    this.vehicleTripUpdateService = vehicleTripUpdateService;
  }

  @Scheduled(fixedRate = 10000)
  public void pollPositionFeed() {
    try {
      byte[] payload = restClient.get().uri("/gtfsr/vehicles").retrieve().body(byte[].class);
      var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
      vehiclePositionService.updatePositions(feed);
    } catch (Exception e) {
      log.error("Failed to poll vehicle positions: {}", e.getMessage());
    }
  }

  @Scheduled(fixedRate = 10000)
  public void pollUpdateFeed() {
    try {
      byte[] payload = restClient.get().uri("/gtfsr/trips").retrieve().body(byte[].class);
      var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
      vehicleTripUpdateService.updateUpdates(feed);
    } catch (Exception e) {
      log.error("Failed to poll trip updates: {}", e.getMessage());
    }
  }
}
