package com.minimall.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minimall.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
