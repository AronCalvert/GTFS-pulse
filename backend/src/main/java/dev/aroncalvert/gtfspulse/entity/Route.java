package dev.aroncalvert.gtfspulse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "routes")
@Getter
@Setter
public class Route {

  @Column(name = "route_id")
  @Id
  String routeId;

  @Column(name = "agency_id")
  String agencyId;

  @Column(name = "route_short_name")
  String routeShortName;

  @Column(name = "route_long_name")
  String routeLongName;

  // @Column(name = "route_desc")

  @Column(name = "route_type")
  int routeType;

  // @Column(name = "route_url")
  // @Column(name = "route_color")
  // @Column(name = "route_text_color")
}
