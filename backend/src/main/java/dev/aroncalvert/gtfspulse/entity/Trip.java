package dev.aroncalvert.gtfspulse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trips")
@Setter
@Getter
public class Trip {

  // route_id service_id trip_id trip_headsign trip_short_name direction_id
  // block_id shape_id

  @Column(name = "route_id")
  String routeId;

  @Column(name = "service_id")
  String serviceId;

  @Id
  @Getter
  @Column(name = "trip_id")
  String tripId;

  @Column(name = "trip_headsign")
  String tripHeadSign;

  @Column(name = "trip_short_name")
  String tripShortName;

  @Column(name = "direction_id")
  int directionId;

  @Column(name = "block_id")
  String blockId;

  @Column(name = "shape_id")
  String shapeId;
}
