package dev.aroncalvert.gtfspulse.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.StopDTO;
import dev.aroncalvert.gtfspulse.entity.Stop;
import dev.aroncalvert.gtfspulse.repository.StopRepository;
import dev.aroncalvert.gtfspulse.service.mapper.StopMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StopService {

  private final StopRepository stopRepository;
  private final StopMapper stopMapper;

  public StopDTO getStopInfo(String stopId) {
    Stop stop = stopRepository.findById(stopId)
        .orElseThrow(() -> new NoSuchElementException("No stop found with id: " + stopId));
    return stopMapper.toDto(stop);
  }

}
