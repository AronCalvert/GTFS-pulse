package dev.aroncalvert.gtfspulse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aroncalvert.gtfspulse.dto.CreateUserDTO;
import dev.aroncalvert.gtfspulse.dto.LoginDTO;
import dev.aroncalvert.gtfspulse.dto.UserDTO;
import dev.aroncalvert.gtfspulse.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(@RequestBody CreateUserDTO dto) {
    return ResponseEntity.ok(authService.register(dto));
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody LoginDTO dto) {
    return ResponseEntity.ok(authService.login(dto));
  }
}
