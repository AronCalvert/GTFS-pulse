package dev.aroncalvert.bustracker;

import dev.aroncalvert.gtfspulse.dto.FavouriteDTO;
import dev.aroncalvert.gtfspulse.dto.FavouriteRequestDTO;
import dev.aroncalvert.gtfspulse.entity.Favourite;
import dev.aroncalvert.gtfspulse.entity.User;
import dev.aroncalvert.gtfspulse.repository.FavouriteRepository;
import dev.aroncalvert.gtfspulse.repository.UserRepository;
import dev.aroncalvert.gtfspulse.service.FavouriteService;
import dev.aroncalvert.gtfspulse.service.mapper.FavouriteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceTest {

  @Mock
  FavouriteRepository favouriteRepository;

  @Mock
  FavouriteMapper favouriteMapper;

  @Mock
  UserRepository userRepository;

  @InjectMocks
  FavouriteService favouriteService;

  private User user;
  private User otherUser;

  @BeforeEach
  void setupSecurityContext() {
    user = new User();
    user.setId(1L);
    user.setEmail("user@test.com");

    otherUser = new User();
    otherUser.setId(2L);
    otherUser.setEmail("other@test.com");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(authentication.getName()).thenReturn("user@test.com");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void getFavourite_withMatchingUser_returnsFavouriteDTO() {
    Favourite favourite = new Favourite();
    favourite.setId(10L);
    favourite.setUserId(1L);
    favourite.setStopId("stop-1");

    FavouriteDTO expectedDto = new FavouriteDTO(10L, 1L, "stop-1", null);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findById(10L)).thenReturn(Optional.of(favourite));
    when(favouriteMapper.toDto(favourite)).thenReturn(expectedDto);

    FavouriteDTO result = favouriteService.getFavourite(10L);

    assertEquals(expectedDto, result);
  }

  @Test
  void getFavourite_withDifferentUser_throwsBadCredentialsException() {
    Favourite favourite = new Favourite();
    favourite.setId(10L);
    favourite.setUserId(2L); // belongs to otherUser

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findById(10L)).thenReturn(Optional.of(favourite));

    assertThrows(BadCredentialsException.class, () -> favouriteService.getFavourite(10L));
  }

  @Test
  void getFavourite_withNonExistentId_throwsNoSuchElementException() {
    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> favouriteService.getFavourite(99L));
  }

  @Test
  void removeFavourite_withMatchingUser_deletesFromRepository() {
    Favourite favourite = new Favourite();
    favourite.setId(10L);
    favourite.setUserId(1L);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findById(10L)).thenReturn(Optional.of(favourite));

    favouriteService.removeFavourite(10L);

    verify(favouriteRepository).deleteById(10L);
  }

  @Test
  void removeFavourite_withDifferentUser_throwsBadCredentialsException() {
    Favourite favourite = new Favourite();
    favourite.setId(10L);
    favourite.setUserId(2L); // belongs to otherUser

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findById(10L)).thenReturn(Optional.of(favourite));

    assertThrows(BadCredentialsException.class, () -> favouriteService.removeFavourite(10L));
    verify(favouriteRepository, never()).deleteById(anyLong());
  }

  @Test
  void setFavourite_withValidRequest_returnsFavouriteDTO() {
    FavouriteRequestDTO request = new FavouriteRequestDTO("stop-1", "route-1");

    Favourite saved = new Favourite();
    saved.setId(10L);
    saved.setUserId(1L);
    saved.setStopId("stop-1");
    saved.setRouteId("route-1");

    FavouriteDTO expectedDto = new FavouriteDTO(10L, 1L, "stop-1", "route-1");

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.save(any(Favourite.class))).thenReturn(saved);
    when(favouriteMapper.toDto(any(Favourite.class))).thenReturn(expectedDto);

    FavouriteDTO result = favouriteService.setFavourite(request);

    assertEquals(expectedDto, result);
    verify(favouriteRepository).save(any(Favourite.class));
  }

  @Test
  void getAllFavourites_withExistingUser_returnsListOfFavouriteDTOs() {
    Favourite fav1 = new Favourite();
    fav1.setId(1L);
    fav1.setUserId(1L);
    fav1.setStopId("stop-1");

    Favourite fav2 = new Favourite();
    fav2.setId(2L);
    fav2.setUserId(1L);
    fav2.setStopId("stop-2");

    FavouriteDTO dto1 = new FavouriteDTO(1L, 1L, "stop-1", null);
    FavouriteDTO dto2 = new FavouriteDTO(2L, 1L, "stop-2", null);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(favouriteRepository.findByUserId(1L)).thenReturn(List.of(fav1, fav2));
    when(favouriteMapper.toDto(fav1)).thenReturn(dto1);
    when(favouriteMapper.toDto(fav2)).thenReturn(dto2);

    List<FavouriteDTO> result = favouriteService.getAllFavourites();

    assertEquals(2, result.size());
    assertEquals(List.of(dto1, dto2), result);
  }
}
