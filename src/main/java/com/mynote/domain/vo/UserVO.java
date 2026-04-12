package com.mynote.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {
    private Long userId;
    private String username;
}
