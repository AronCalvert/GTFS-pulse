package dev.aroncalvert.gtfspulse.repository;

import dev.aroncalvert.gtfspulse.entity.StopTimeId;
import dev.aroncalvert.gtfspulse.entity.Trip;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.Stop;
import dev.aroncalvert.gtfspulse.entity.StopTime;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, StopTimeId> {
  List<StopTime> findByTripOrderByStopSequenceAsc(Trip trip);

  List<StopTime> findByTripAndStopSequenceGreaterThanOrderByStopSequenceAsc(Trip trip, int stopSequence);

  List<StopTime> findByTripAndStopSequenceLessThanEqualOrderByStopSequenceAsc(Trip trip, int stopSequence);

  Optional<StopTime> findFirstByTripAndStopSequenceGreaterThanOrderByStopSequenceAsc(Trip trip, int stopSequence);

  Optional<StopTime> findByTripAndStop(Trip trip, Stop stop);
}
