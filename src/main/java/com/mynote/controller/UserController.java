package com.mynote.controller;

import com.mynote.common.Result;
import com.mynote.domain.dto.UserDto;
import com.mynote.domain.po.User;
import com.mynote.domain.vo.UserLoginVO;
import com.mynote.domain.vo.UserVO;
import com.mynote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserVO> register(@RequestBody @Validated UserDto userDto) {
        UserVO userVO = userService.register(userDto);
        return Result.success(userVO);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<UserLoginVO> login(@RequestBody @Validated UserDto userDto) {
        return Result.success(userService.login(userDto));
    }
}
