package dev.aroncalvert.gtfspulse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stop_times")
@IdClass(StopTimeId.class)
@Setter
@Getter
public class StopTime {

  @ManyToOne
  @JoinColumn(name = "trip_id")
  @Id
  Trip trip;

  @Column(name = "arrival_time")
  int arrivalTime;

  @Column(name = "departure_time")
  int departureTime;

  @ManyToOne
  @JoinColumn(name = "stop_id")
  Stop stop;

  @Column(name = "stop_sequence")
  @Id
  int stopSequence;

  @Column(name = "stop_headsign", nullable = true)
  String stopHeadsign;

  @Column(name = "pickup_type")
  int pickupType;

  @Column(name = "drop_off_type")
  int dropOffType;

  @Column(name = "timepoint")
  int timepoint;
}
