package com.mynote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mynote.common.BusinessException;
import com.mynote.domain.po.Category;
import com.mynote.mapper.CategoryMapper;
import com.mynote.service.CategoryService;
import com.mynote.util.UserContext;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public Category createCategory(String name) {
        //1.校验参数是否正确
        if (name == null) {
            throw new BusinessException("分类名称不能为空");
        }
        //2.创建分类
        Category category = Category.builder()
                .name(name)
                .userId(UserContext.getUser())
                .build();
        //3.保存分类
        save(category);
        //4.返回 分类
        return category;
    }

}
