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
public class NoteVO {
    private Long id;
    private String title;
    private Integer viewCount;
    private LocalDateTime updateTime;
    private Long userId;
}
