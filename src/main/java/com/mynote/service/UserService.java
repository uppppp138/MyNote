package com.mynote.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mynote.domain.dto.UserDto;
import com.mynote.domain.po.User;
import com.mynote.domain.vo.UserLoginVO;
import com.mynote.domain.vo.UserVO;

public interface UserService extends IService<User> {
    UserVO register(UserDto userDto);

    UserLoginVO login(UserDto userDto);
}
