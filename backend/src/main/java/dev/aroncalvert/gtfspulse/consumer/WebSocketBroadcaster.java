package dev.aroncalvert.gtfspulse.consumer;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.BusData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketBroadcaster {

  private final SimpMessagingTemplate simpMessagingTemplate;

  @KafkaListener(id = "myId", topics = "bus-positions", batch = "true")
  public void listen(List<BusData> busData) {
    simpMessagingTemplate.convertAndSend("/topic/buses", busData);
  }
}
