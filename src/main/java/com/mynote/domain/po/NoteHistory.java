package com.mynote.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("note_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;        // 原笔记ID
    
    private String title;       // 历史标题
    
    private String content;     // 历史内容
    
    private Integer version;    // 版本号
    
    private Long userId;        // 用户ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 修改时间
}