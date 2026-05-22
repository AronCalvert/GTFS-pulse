package dev.aroncalvert.gtfs_ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GtfsIngestorApplication {

  public static void main(String[] args) {
    SpringApplication.run(GtfsIngestorApplication.class, args);
  }

}
