package com.cplan.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity mapped to t_user table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key (auto-increment). */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Username (unique display name). */
    private String username;

    /** Email address (unique, used for login). */
    private String email;

    /** BCrypt-hashed password. */
    private String passwordHash;

    /** Avatar image URL (optional). */
    private String avatarUrl;

    /**
     * Account status.
     * 0 = disabled, 1 = active.
     */
    private Integer status;

    /** Record creation timestamp. */
    private LocalDateTime createdAt;

    /** Record last-update timestamp. */
    private LocalDateTime updatedAt;

    /**
     * Logical deletion flag (MyBatis-Plus @TableLogic).
     * 0 = not deleted, 1 = deleted.
     */
    @TableLogic
    private Integer isDeleted;
}
