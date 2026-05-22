package dev.aroncalvert.gtfspulse.dto;

public record CreateUserDTO(
    String password,
    String userName,
    String email) {
}
