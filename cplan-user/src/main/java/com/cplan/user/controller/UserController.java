package com.cplan.user.controller;

import com.cplan.common.result.Result;
import com.cplan.user.dto.LoginRequest;
import com.cplan.user.dto.LoginResponse;
import com.cplan.user.dto.RegisterRequest;
import com.cplan.user.dto.UserDTO;
import com.cplan.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User REST controller — registration, login, profile retrieval.
 */
@RestController
@RequestMapping("/api/user/v1")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user account.
     * POST /api/user/v1/register
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        log.info("Register request: email={}", req.getEmail());
        userService.register(req);
        return Result.ok();
    }

    /**
     * User login — returns JWT access token.
     * POST /api/user/v1/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("Login request: email={}", req.getEmail());
        LoginResponse response = userService.login(req);
        return Result.ok(response);
    }

    /**
     * Get the currently authenticated user's information.
     * GET /api/user/v1/info
     * Requires X-User-Id header injected by the gateway.
     */
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        log.debug("Get user info: userId={}", userId);
        UserDTO user = userService.getUserById(Long.parseLong(userId));
        return Result.ok(user);
    }
}
