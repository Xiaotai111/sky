package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName:ReportServiceImpl
 * Package:com.sky.service.impl
 * Description:
 *
 * @Date:2023/8/31 17:00
 * @Author:QiTao
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 营销额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //先获取时间列表
        List<LocalDate> list = new ArrayList<>();

        list.add(begin);
        LocalDate t = begin;
        while (!t.equals(end)){
            t = t.plusDays(1);
            list.add(t);
        }
        //根据时间获得当日营销额
        //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate localDate : list) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map mp = new HashMap();
            mp.put("begin", beginTime);
            mp.put("end", endTime);
            mp.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(mp);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //将这两个转为返回值所需格式放入vo后返回


        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(list, ","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //先获取时间列表
        List<LocalDate> list = new ArrayList<>();

        list.add(begin);
        LocalDate t = begin;
        while (!t.equals(end)){
            t = t.plusDays(1);
            list.add(t);
        }
        //根据时间获得 新增用户数列表，以逗号分隔和总用户量列表，以逗号分隔
        //select count(id) from user where order_time > ? and order_time < ?
        //select count(id) from user where order_time < ?
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        for (LocalDate localDate : list) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map mp = new HashMap();
            mp.put("end", endTime);
            Double totalUser = userMapper.countByMap(mp);
            mp.put("begin", beginTime);
            Double newUser = userMapper.countByMap(mp);
            totalUserList.add(totalUser.toString());
            newUserList.add(newUser.toString());

        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(list, ","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //时间列表
        List<LocalDate> list = new ArrayList<>();

        list.add(begin);
        LocalDate t = begin;
        while (!t.equals(end)){
            t = t.plusDays(1);
            list.add(t);
        }
        //订单总数列表 和 有效订单数列表
        List<Integer> totalList = new ArrayList<>();
        List<Integer> validList = new ArrayList<>();
        for (LocalDate localDate : list) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map mp = new HashMap();
            mp.put("end", endTime);
            mp.put("begin", beginTime);
            //select count(id) from orders where order_time > ? and order_time < ?
            Integer total = orderMapper.countByMap(mp);
            mp.put("status", Orders.COMPLETED);
            //select count(id) from orders where order_time > ? and order_time < ? and status = 5
            Integer valid = orderMapper.countByMap(mp);
            totalList.add(total);
            validList.add(valid);

        }
        //根据订单数求订单总数，根据有效订单数求有效订单总数，再计算订单完成率

        Integer all = totalList.stream().reduce(Integer::sum).get();
        Integer validAll = validList.stream().reduce(Integer::sum).get();
        Double rate;
        if(all == 0){
            rate = 0.0;
        }else {
            rate = validAll.doubleValue()/all;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .orderCompletionRate(rate)
                .orderCountList(StringUtils.join(totalList,","))
                .totalOrderCount(all)
                .validOrderCountList(StringUtils.join(validList,","))
                .validOrderCount(validAll)
                .build();
    }

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names,",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
