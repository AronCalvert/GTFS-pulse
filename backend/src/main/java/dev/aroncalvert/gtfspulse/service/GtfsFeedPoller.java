package dev.aroncalvert.gtfspulse.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.google.transit.realtime.GtfsRealtime;

@Service
public class GtfsFeedPoller {
  private final RestClient restClient;
  private final BusPositionService busPositionService;

  public GtfsFeedPoller(RestClient.Builder restClientBuilder, BusPositionService busPositionService) {
    this.restClient = restClientBuilder.baseUrl("http://localhost:5000").build();
    this.busPositionService = busPositionService;
  }

  @Scheduled(fixedRate = 10000)
  public void pollFeed() {
    byte[] payload = restClient.get().uri("/gtfsr").retrieve().body(byte[].class);
    try {
      var feed = GtfsRealtime.FeedMessage.parseFrom(payload);
      busPositionService.updatePositions(feed);
    } catch (Exception e) {
      System.out.println("Feed parse failed" + e.getMessage());
    }
  }
}
