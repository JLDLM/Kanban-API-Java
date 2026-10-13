package com.example.kambaapijava.dto;

import jakarta.validation.constraints.*;

import java.time.*;

public final class Dtos {
    private Dtos() {
    }

    public record Register(@NotBlank @Size(max = 80) String username, @NotBlank @Email @Size(max = 160) String email,
                           @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$") String password) {
    }

    public record Login(@NotBlank String username, @NotBlank String password) {
    }

    public record Refresh(@NotBlank String refreshToken) {
    }

    public record Tokens(String accessToken, String refreshToken, String tokenType) {
        public Tokens(String a, String r) {
            this(a, r, "Bearer");
        }
    }

    public record TaskInput(@NotBlank @Size(max = 180) String title, @Size(max = 5000) String description,
                            @Pattern(regexp = "TODO|IN_PROGRESS|DONE") String status,
                            @Pattern(regexp = "LOW|MEDIUM|HIGH") String priority, @Future LocalDate dueDate,
                            @NotNull Long boardId, Long assigneeId) {
    }

    public record TaskStatus(@NotBlank @Pattern(regexp = "TODO|IN_PROGRESS|DONE") String status) {
    }

    public record BoardInput(@NotBlank @Size(max = 120) String name) {
    }

    public record RoleInput(@NotBlank @Pattern(regexp = "ADMIN|USER") String role) {
    }

    public record ActiveInput(boolean active) {
    }

    public record Page<T>(java.util.List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record Task(long id, String title, String description, String status, String priority, LocalDate dueDate,
                       long boardId, Long assigneeId, long createdBy, Instant createdAt, Instant updatedAt) {
    }

    public record Board(long id, String name, long ownerId, Instant createdAt) {
    }

    public record UserView(long id, String username, String email, String role, boolean active, Instant createdAt) {
    }
}
