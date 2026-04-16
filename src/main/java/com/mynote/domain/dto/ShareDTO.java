package com.mynote.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareDTO {
    private String shareCode;
    private String shareUrl;
    private String expireTime;
}