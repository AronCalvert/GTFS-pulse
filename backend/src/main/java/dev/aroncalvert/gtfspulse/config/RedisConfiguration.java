package dev.aroncalvert.gtfspulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import dev.aroncalvert.gtfspulse.dto.VehicleData;

@Configuration
public class RedisConfiguration {

  @Bean
  public RedisTemplate<String, VehicleData> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, VehicleData> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JacksonJsonRedisSerializer<>(VehicleData.class));
    return template;
  }
}
