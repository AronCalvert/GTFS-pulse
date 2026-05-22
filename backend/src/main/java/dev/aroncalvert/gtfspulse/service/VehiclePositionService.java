package dev.aroncalvert.gtfspulse.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import dev.aroncalvert.gtfspulse.dto.VehicleData;

@Service
@RequiredArgsConstructor
public class VehiclePositionService {
  private final RedisTemplate<String, VehicleData> redisTemplate;

  public VehicleData getPosition(String tripId) {
    return redisTemplate.opsForValue().get("vehicle:" + tripId);
  }
}
