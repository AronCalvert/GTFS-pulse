package dev.aroncalvert.gtfspulse.consumer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import dev.aroncalvert.gtfspulse.dto.VehicleData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisListener {

  private final RedisTemplate<String, VehicleData> redisTemplate;

  @KafkaListener(id = "redis", topics = "vehicle-positions", batch = "true")
  public void listen(List<VehicleData> vehicleDataList) {
    vehicleDataList.stream()
        .forEach(vehicleData -> redisTemplate.opsForValue().set("vehicle:" + vehicleData.tripId(), vehicleData, 30, TimeUnit.SECONDS));
  }
}
