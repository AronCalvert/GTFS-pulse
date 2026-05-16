package dev.aroncalvert.gtfspulse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stops")
@Setter
public class Stop {

  // stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station

  @Column(name = "stop_id")
  @Getter
  @Id
  String stopId;

  @Column(name = "stop_code")
  String stopCode;

  @Column(name = "stop_name")
  String stopName;

  // @Column(name = "stop_desc", nullable = true)

  @Column(name = "stop_lat")
  double stopLat;

  @Column(name = "stop_lon")
  double stopLon;

  // @Column(name = "zone_id", nullable = true)
  // @Column(name = "stop_url", nullable = true)
  // @Column(name = "location_type", nullable = true)
  // @Column(name = "parent_station", nullable= true)

}
