package dev.aroncalvert.gtfs_ingestor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.google.transit.realtime.GtfsRealtime;

@Service
public class GtfsFeedPoller {
  private final RestClient restClient;
  private final VehiclePositionService vehiclePositionService;

  public GtfsFeedPoller(RestClient.Builder restClientBuilder, VehiclePositionService vehiclePositionService,
      @Value("${gtfs.simulator.url:http://localhost:5000}") String simulatorUrl) {
    this.restClient = restClientBuilder.baseUrl(simulatorUrl).build();
    this.vehiclePositionService = vehiclePositionService;
  }

  @Scheduled(fixedRate = 10000)
  public void pollFeed() {
    try {
      byte[] payload = restClient.get().uri("/gtfsr").retrieve().body(byte[].class);
      var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
      vehiclePositionService.updatePositions(feed);
    } catch (Exception e) {
    }
  }
}
