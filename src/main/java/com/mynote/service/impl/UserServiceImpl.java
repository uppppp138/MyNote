package com.mynote.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mynote.common.BusinessException;
import com.mynote.domain.dto.UserDto;
import com.mynote.domain.po.User;
import com.mynote.domain.vo.UserLoginVO;
import com.mynote.domain.vo.UserVO;
import com.mynote.mapper.UserMapper;
import com.mynote.service.UserService;
import com.mynote.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserVO register(UserDto userDto) {
        //1.用户是否存在
        if (userDto == null) {
            throw new BusinessException(400,"用户信息不能为空");
        }
        //2.根据username查询用户是否存在
        if (getByUsername(userDto.getUsername())!=null) {
            //存在
            throw new BusinessException(400,"用户已存在");
        }
        //3.创建用户
        User user = BeanUtil.copyProperties(userDto, User.class);
        //4.密码加密
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        //5.保存用户
        save(user);
        return UserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     *
     * @param userDto
     * @return token
     */
    @Override
    public UserLoginVO login(UserDto userDto) {
        //1.判断userDto是否为空
        if (userDto == null) {
            throw new BusinessException(400,"用户信息不能为空");
        }
        //2.根据username查询用户是否存在
        User user = getByUsername(userDto.getUsername());
        if (user == null) {
            throw new BusinessException(401,"用户不存在，请注册后登录");
        }
        //3.密码校验
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        //4.生成token
        String token = jwtUtil.generateToken(user.getId());
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
        return userLoginVO;
    }

    /**
     * 根据username查询用户
     */
    private User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        //存在返回对应用户，不存在返回null
        User user = getOne(wrapper, false);
        return user;
    }
}
