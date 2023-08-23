package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName:SetmealDishMapper
 * Package:com.sky.mapper
 * Description:
 *
 * @Date:2023/8/23 20:40
 * @Author:QiTao
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);
}
