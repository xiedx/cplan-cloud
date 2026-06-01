package com.cplan.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.common.util.JwtUtil;
import com.cplan.user.dto.LoginRequest;
import com.cplan.user.dto.LoginResponse;
import com.cplan.user.dto.RegisterRequest;
import com.cplan.user.dto.UserDTO;
import com.cplan.user.entity.User;
import com.cplan.user.mapper.UserMapper;
import com.cplan.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of UserService.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** Token expiry — 24 hours. */
    private static final long EXPIRE_SECONDS = 86400L;

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest req) {
        // Check email uniqueness
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (emailCount != null && emailCount > 0) {
            throw new BizException(ResultCode.EMAIL_ALREADY_REGISTERED);
        }

        // Check username uniqueness
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );
        if (usernameCount != null && usernameCount > 0) {
            throw new BizException(ResultCode.USER_EXIST);
        }

        // Persist new user with hashed password
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .status(1)
                .isDeleted(0)
                .build();
        userMapper.insert(user);
        log.info("New user registered: email={}, id={}", user.getEmail(), user.getId());
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // Look up user by email
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        // Verify password
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // Check account is active
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.USER_DISABLED);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("User logged in: userId={}", user.getId());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(EXPIRE_SECONDS)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
