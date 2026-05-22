package dev.aroncalvert.gtfspulse.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.aroncalvert.gtfspulse.dto.CreateUserDTO;
import dev.aroncalvert.gtfspulse.dto.LoginDTO;
import dev.aroncalvert.gtfspulse.dto.UserDTO;
import dev.aroncalvert.gtfspulse.entity.User;
import dev.aroncalvert.gtfspulse.repository.UserRepository;
import dev.aroncalvert.gtfspulse.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder encoder;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final JWTService jwtService;

  public UserDTO register(CreateUserDTO dto) {
    String hashedPassword = encoder.encode(dto.password());
    User user = userMapper.toUserEntity(dto, hashedPassword);
    User saved = userRepository.save(user);
    return userMapper.toUserDTO(saved);
  }

  public String login(LoginDTO dto) {
    User user = userRepository.findByEmail(dto.email())
        .orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));
    if (encoder.matches(dto.password(), user.getHashedPassword())) {
      return jwtService.generateToken(dto.email());
    } else {
      throw new BadCredentialsException("Invalid Credentials");
    }
  }
}
