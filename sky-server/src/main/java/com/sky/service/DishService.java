package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * ClassName:DishService
 * Package:com.sky.service
 * Description:
 *
 * @Date:2023/8/23 15:44
 * @Author:QiTao
 */
public interface DishService {
    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
