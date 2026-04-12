package com.mynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mynote.domain.po.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
