package dev.aroncalvert.gtfspulse.service.mapper;

import org.springframework.stereotype.Component;

import dev.aroncalvert.gtfspulse.dto.CreateUserDTO;
import dev.aroncalvert.gtfspulse.dto.UserDTO;
import dev.aroncalvert.gtfspulse.entity.User;
import dev.aroncalvert.gtfspulse.entity.Role;

@Component
public class UserMapper {

  public User toUserEntity(CreateUserDTO dto, String hashedPassword) {
    var user = new User();
    user.setEmail(dto.email());
    user.setHashedPassword(hashedPassword);
    user.setName(dto.userName());
    user.setRole(Role.USER);
    return user;
  }

  public UserDTO toUserDTO(User user) {
    return new UserDTO(
        user.getName(),
        user.getEmail());
  }
}
