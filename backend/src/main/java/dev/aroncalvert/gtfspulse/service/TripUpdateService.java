package dev.aroncalvert.gtfspulse.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.TripUpdateData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripUpdateService {
  private final RedisTemplate<String, TripUpdateData> redisTemplate;

  public TripUpdateData getTripUpdate(String tripId) {
    return redisTemplate.opsForValue().get("update:" + tripId);
  }
}
