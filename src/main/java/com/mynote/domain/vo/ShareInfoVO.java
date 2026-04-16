package com.mynote.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfoVO {
    private String shareCode;
    private String shareUrl;
    private LocalDateTime expireTime;
}
