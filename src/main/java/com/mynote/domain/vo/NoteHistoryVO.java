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
public class NoteHistoryVO {
    private Long id;
    private Long noteId;
    private String title;
    private String content;
    private Integer version;
    private Long userId;
    private LocalDateTime createTime;
}