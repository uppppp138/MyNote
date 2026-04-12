package com.mynote.controller;


import com.mynote.common.Result;
import com.mynote.domain.po.Category;
import com.mynote.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    /**
     * 创建分类
     */
    @PostMapping("/create")
    @Operation(summary = "创建分类")
    public Result<Category> createCategory(@RequestParam String name) {
        return Result.success(categoryService.createCategory(name));
    }
}
