package dev.aroncalvert.gtfspulse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agency")
public class Agency {

  @Column(name = "agency_id")
  @Id
  String agencyId;

  @Column(name = "agency_name")
  String agencyName;

  @Column(name = "agency_url")
  String agencyUrl;

  @Column(name = "agency_timezone")
  String agencyTimezone;
}
