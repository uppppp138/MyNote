package com.mynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mynote.domain.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
