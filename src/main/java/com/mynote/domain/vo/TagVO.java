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
public class TagVO {
    private Long id;
    private String name;
    private Integer noteCount;
    private LocalDateTime createTime;
}