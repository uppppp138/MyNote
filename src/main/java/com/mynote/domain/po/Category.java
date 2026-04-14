package com.mynote.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName("category")
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long userId;
}
