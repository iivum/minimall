package com.minimall.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minimall.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
