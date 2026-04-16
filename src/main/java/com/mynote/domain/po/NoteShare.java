package com.mynote.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("note_share")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteShare {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("note_id")
    private Long noteId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("share_code")
    private String shareCode;
    
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField("is_enabled")
    private Integer isEnabled;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}