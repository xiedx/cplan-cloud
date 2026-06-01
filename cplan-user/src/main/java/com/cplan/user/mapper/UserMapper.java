package com.cplan.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cplan.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User mapper extending MyBatis-Plus BaseMapper.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
