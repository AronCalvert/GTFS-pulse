package dev.aroncalvert.gtfspulse.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.FavouriteDTO;
import dev.aroncalvert.gtfspulse.dto.FavouriteRequestDTO;
import dev.aroncalvert.gtfspulse.entity.Favourite;
import dev.aroncalvert.gtfspulse.entity.User;
import dev.aroncalvert.gtfspulse.repository.FavouriteRepository;
import dev.aroncalvert.gtfspulse.repository.UserRepository;
import dev.aroncalvert.gtfspulse.service.mapper.FavouriteMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavouriteService {

  private final FavouriteRepository favouriteRepository;
  private final FavouriteMapper favouriteMapper;
  private final UserRepository userRepository;

  public FavouriteDTO setFavourite(FavouriteRequestDTO dto) {
    User user = getSecurityContextUser();
    var favourite = new Favourite();
    favourite.setUserId(user.getId());
    favourite.setRouteId(dto.routeId());
    favourite.setStopId(dto.stopId());
    favouriteRepository.save(favourite);
    return favouriteMapper.toDto(favourite);
  }

  public FavouriteDTO getFavourite(long id) {
    User user = getSecurityContextUser();
    Favourite favourite = favouriteRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("not found"));

    if (Objects.equals(user.getId(), favourite.getUserId())) {
      return favouriteMapper.toDto(favourite);
    } else {
      throw new BadCredentialsException("can't get other peoples favourites");
    }
  }

  public List<FavouriteDTO> getAllFavourites() {
    User user = getSecurityContextUser();
    List<Favourite> favourites = favouriteRepository.findByUserId(user.getId());
    return favourites.stream().map(favouriteMapper::toDto).collect(Collectors.toList());
  }

  public void removeFavourite(long id) {
    User user = getSecurityContextUser();
    Favourite favourite = favouriteRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("not found"));

    if (Objects.equals(user.getId(), favourite.getUserId())) {
      favouriteRepository.deleteById(id);
    } else {
      throw new BadCredentialsException("can't delete other peoples favourites");
    }
  }

  private User getSecurityContextUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new NoSuchElementException("Not found"));
  }
}
