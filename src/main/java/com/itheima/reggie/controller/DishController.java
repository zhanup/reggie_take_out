package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {


    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto DishDto
     * @return Result
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWitchFlavor(dishDto);

        return Result.success("新增菜品成功");
    }

    /**
     * 菜品分页
     * @param page int
     * @param pageSize int
     * @param name String
     * @return String Result
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map(item -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品和菜品口味信息
     * @param id Long
     * @return Result
     */
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto DishDto
     * @return Result
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWitchFlavor(dishDto);

        // 清理所有菜品的缓存数据
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);

        // 删除某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("修改菜品成功");
    }

    /**
     * 根据菜品查询对应数据
     * @param dish Dish
     * @return Result
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 线程Redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果存在，直接返回，无需查询数据库
        if (dishDtoList != null) {
            return Result.success(dishDtoList);
        }
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        queryWrapper.eq(Dish::getStatus, 1);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map(item -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor>dishFlavorList =  dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return Result.success(dishDtoList);
    }

}
