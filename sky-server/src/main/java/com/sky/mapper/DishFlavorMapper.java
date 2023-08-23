package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName:DishFlavorMapper
 * Package:com.sky.mapper
 * Description:
 *
 * @Date:2023/8/23 16:06
 * @Author:QiTao
 */
@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);
}
