package dev.aroncalvert.gtfspulse.consumer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.TripUpdateData;
import dev.aroncalvert.gtfspulse.dto.VehicleData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisListener {

  private final RedisTemplate<String, VehicleData> vehicleUpdateTemplate;
  private final RedisTemplate<String, TripUpdateData> tripUpdateTemplate;

  @KafkaListener(id = "redis-vehicle-positions", topics = "vehicle-positions", batch = "true")
  public void positionListener(List<VehicleData> vehicleDatas) {
    vehicleDatas.stream()
        .forEach(
            vehicleData -> vehicleUpdateTemplate.opsForValue().set("vehicle:" + vehicleData.tripId(), vehicleData, 30,
                TimeUnit.SECONDS));
  }

  @KafkaListener(id = "redis-trip-updates", topics = "trip-updates", batch = "true", containerFactory = "tripUpdateContainerFactory")
  public void updateListener(List<TripUpdateData> tripUpdates) {
    tripUpdates.stream()
        .forEach(tripUpdate -> tripUpdateTemplate.opsForValue().set("update:" + tripUpdate.tripId(), tripUpdate, 30,
            TimeUnit.SECONDS));
  }
}
