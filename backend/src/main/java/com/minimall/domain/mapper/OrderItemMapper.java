package com.minimall.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minimall.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
