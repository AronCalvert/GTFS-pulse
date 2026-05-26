package dev.aroncalvert.gtfspulse.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import dev.aroncalvert.gtfspulse.dto.TripUpdateData;

@Configuration
public class KafkaConfiguration {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ConsumerFactory<String, TripUpdateData> tripUpdateConsumerFactory() {
    JsonDeserializer<TripUpdateData> deserializer = new JsonDeserializer<>(TripUpdateData.class);
    deserializer.addTrustedPackages("*");
    deserializer.setUseTypeHeaders(false);

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "gtfs-pulse-trip-updates");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TripUpdateData> tripUpdateContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, TripUpdateData> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(tripUpdateConsumerFactory());
    factory.setBatchListener(true);
    return factory;
  }
}
