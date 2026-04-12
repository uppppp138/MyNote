package com.mynote.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note")
@Builder
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long categoryId;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
