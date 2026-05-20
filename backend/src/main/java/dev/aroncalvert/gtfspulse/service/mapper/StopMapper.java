package dev.aroncalvert.gtfspulse.service.mapper;

import dev.aroncalvert.gtfspulse.dto.StopDTO;
import dev.aroncalvert.gtfspulse.entity.Stop;

public class StopMapper {
  public StopDTO toDto(Stop stop) {
    return new StopDTO(
        stop.getStopId(),
        stop.getStopCode(),
        stop.getStopName(),
        stop.getStopLat(),
        stop.getStopLon());
  }
}
