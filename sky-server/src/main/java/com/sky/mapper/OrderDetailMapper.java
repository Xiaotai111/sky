package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName:OrderDetailMapper
 * Package:com.sky.mapper
 * Description:
 *
 * @Date:2023/8/28 20:36
 * @Author:QiTao
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
}
