package com.cplan.user.service;

import com.cplan.user.dto.LoginRequest;
import com.cplan.user.dto.LoginResponse;
import com.cplan.user.dto.RegisterRequest;
import com.cplan.user.dto.UserDTO;

/**
 * User service interface.
 */
public interface UserService {

    /**
     * Register a new user.
     *
     * @param req registration details
     */
    void register(RegisterRequest req);

    /**
     * Authenticate user and return JWT token.
     *
     * @param req login credentials
     * @return login response with JWT
     */
    LoginResponse login(LoginRequest req);

    /**
     * Retrieve user information by ID.
     *
     * @param userId the user's primary key
     * @return user data transfer object
     */
    UserDTO getUserById(Long userId);
}
