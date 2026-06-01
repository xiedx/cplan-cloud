package com.cplan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User context DTO extracted from gateway-injected headers.
 * Downstream services read this from X-User-Id / X-Username request headers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContextDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** User ID from JWT subject claim */
    private Long userId;

    /** Username from JWT claims */
    private String username;
}
