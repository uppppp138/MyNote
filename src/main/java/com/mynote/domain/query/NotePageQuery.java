package com.mynote.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品分页查询条件")
public class NotePageQuery extends PageQuery {
    @Schema(description ="搜索关键字")
    private Long categoryId;
    @Schema(description ="商品分类")
    private String keyword;
}