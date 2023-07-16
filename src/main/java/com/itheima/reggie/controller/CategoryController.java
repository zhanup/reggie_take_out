package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category Category
     * @return Result
     */
    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("新增分类成功");
    }

    /**
     * 列表数据
     * @param page Integer
     * @param pageSize Integer
     * @return Result
     */
    @GetMapping("/page")
    public Result<Page<Category>> page(Integer page, Integer pageSize) {
        // 构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * @param ids Long
     * @return Result
     */
    @DeleteMapping
    public Result<String> delete(Long ids) {
        categoryService.remove(ids);
        return Result.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息
     * @param category Category
     * @return Result
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类
     * @param category Category
     * @return Result
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);
        List<Category> list =  categoryService.list(queryWrapper);
        return Result.success(list);
    }

}
