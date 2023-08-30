package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName:OrdersTask
 * Package:com.sky.task
 * Description:
 *
 * @Date:2023/8/30 10:40
 * @Author:QiTao
 */
@Component
@Slf4j
public class OrdersTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("超时订单自动取消：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //select * from orders where status = ? and orders_time < ?(当前时间减去十五分钟)
        List<Orders> list = orderMapper.getByStatusAndOrderTimeTL(Orders.PENDING_PAYMENT,time);

        if(list != null && list.size()>0){
            for (Orders orders : list) {
                orders.setCancelTime(LocalDateTime.now());
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理派送中的订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //select * from orders where status = ? and orders_time < ?(当前时间减去六十分钟)
        List<Orders> list = orderMapper.getByStatusAndOrderTimeTL(Orders.DELIVERY_IN_PROGRESS,time);

        if(list != null && list.size()>0){
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
