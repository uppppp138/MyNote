package com.mynote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mynote.domain.po.Category;

public interface CategoryService extends IService<Category> {

    Category createCategory(String name);
}
