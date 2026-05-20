package dev.aroncalvert.gtfspulse.service.mapper;

import org.springframework.stereotype.Component;

import dev.aroncalvert.gtfspulse.dto.StopTimeDTO;
import dev.aroncalvert.gtfspulse.entity.StopTime;

@Component
public class StopTimeMapper {

  public StopTimeDTO toDto(StopTime stopTime) {
    return new StopTimeDTO(
        stopTime.getTrip().getTripId(),
        stopTime.getArrivalTime(),
        stopTime.getDepartureTime(),
        stopTime.getStop().getStopId(),
        stopTime.getStopSequence(),
        stopTime.getStopHeadsign(),
        stopTime.getPickupType(),
        stopTime.getDropOffType(),
        stopTime.getTimepoint());
  }
}
