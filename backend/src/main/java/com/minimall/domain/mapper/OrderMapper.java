package com.minimall.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minimall.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
