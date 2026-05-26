package dev.aroncalvert.gtfspulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import dev.aroncalvert.gtfspulse.dto.TripUpdateData;
import dev.aroncalvert.gtfspulse.dto.VehicleData;

@Configuration
public class RedisConfiguration {

  @Bean
  public RedisTemplate<String, VehicleData> vehiclePositionsTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, VehicleData> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JacksonJsonRedisSerializer<>(VehicleData.class));
    return template;
  }

  @Bean
  public RedisTemplate<String, TripUpdateData> tripUpdatesTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, TripUpdateData> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JacksonJsonRedisSerializer<>(TripUpdateData.class));
    return template;
  }
}
