package dev.aroncalvert.gtfspulse.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aroncalvert.gtfspulse.dto.FavouriteDTO;
import dev.aroncalvert.gtfspulse.dto.FavouriteRequestDTO;
import dev.aroncalvert.gtfspulse.service.FavouriteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/favourite")
@RequiredArgsConstructor
public class FavouriteController {

  private final FavouriteService favouriteService;

  @PostMapping("/set")
  public ResponseEntity<FavouriteDTO> setFavourite(@RequestBody FavouriteRequestDTO dto) {
    return ResponseEntity.ok(favouriteService.setFavourite(dto));
  }

  @GetMapping("/all")
  public ResponseEntity<List<FavouriteDTO>> getAllFavourites() {
    return ResponseEntity.ok(favouriteService.getAllFavourites());
  }

  @GetMapping("/{id}")
  public ResponseEntity<FavouriteDTO> getFavourite(@PathVariable("id") long id) {
    return ResponseEntity.ok(favouriteService.getFavourite(id));
  }

  @DeleteMapping("/{id}")
  public void deleteFavourite(@PathVariable("id") long id) {
    favouriteService.removeFavourite(id);
  }

}
