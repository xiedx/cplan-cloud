package com.cplan.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned on successful login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT access token. */
    private String accessToken;

    /** Token type: always "Bearer". */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Token expiry duration in seconds. */
    private Long expiresIn;

    /** Authenticated user ID. */
    private Long userId;

    /** Authenticated username. */
    private String username;
}
