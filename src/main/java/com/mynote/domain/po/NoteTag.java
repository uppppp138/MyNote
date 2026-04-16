package com.mynote.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("note_tag")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("note_id")
    private Long noteId;
    
    @TableField("tag_id")
    private Long tagId;
}