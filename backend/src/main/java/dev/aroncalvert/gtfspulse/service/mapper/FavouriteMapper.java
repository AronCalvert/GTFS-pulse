package dev.aroncalvert.gtfspulse.service.mapper;

import org.springframework.stereotype.Component;

import dev.aroncalvert.gtfspulse.dto.FavouriteDTO;
import dev.aroncalvert.gtfspulse.entity.Favourite;

@Component
public class FavouriteMapper {

  public FavouriteDTO toDto(Favourite favourite) {
    return new FavouriteDTO(
        favourite.getId(),
        favourite.getUserId(),
        favourite.getStopId(),
        favourite.getRouteId());
  }
}
