package dev.aroncalvert.gtfspulse.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.StopTimeDTO;
import dev.aroncalvert.gtfspulse.entity.StopTime;
import dev.aroncalvert.gtfspulse.entity.Trip;
import dev.aroncalvert.gtfspulse.repository.StopTimeRepository;
import dev.aroncalvert.gtfspulse.repository.TripRepository;
import dev.aroncalvert.gtfspulse.service.mapper.StopTimeMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

  private final StopTimeRepository stopTimeRepository;
  private final TripRepository tripRepository;
  private final StopTimeMapper stopTimeMapper;

  public List<StopTimeDTO> getStopsInOrder(String tripId) {
    Trip trip = getTripById(tripId);
    List<StopTime> stopTimes = stopTimeRepository.findByTripOrderByStopSequenceAsc(trip);
    return stopTimes.stream()
        .map(stopTimeMapper::toDto)
        .toList();
  }

  public Trip getTripById(String tripId) {
    return tripRepository.findById(tripId)
        .orElseThrow(() -> new NoSuchElementException("No trip found for trip:" + tripId));
  }

  public List<StopTimeDTO> getStopsPassed(String tripId, int stopSequence) {
    Trip trip = getTripById(tripId);
    List<StopTime> stopsPassed = stopTimeRepository
        .findByTripAndStopSequenceLessThanEqualOrderByStopSequenceAsc(trip, stopSequence);
    return stopsPassed.stream()
        .map(stopTimeMapper::toDto)
        .toList();
  }

  public StopTimeDTO getNextStop(String tripId, int stopSequence) {
    Trip trip = getTripById(tripId);
    StopTime stopTime = stopTimeRepository
        .findFirstByTripAndStopSequenceGreaterThanOrderByStopSequenceAsc(trip, stopSequence)
        .orElseThrow(() -> new NoSuchElementException("No next stop time for trip " + tripId));
    return stopTimeMapper.toDto(stopTime);
  }
}
