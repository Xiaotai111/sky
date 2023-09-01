package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkspaceService workspaceService;
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
            Integer totalUser = userMapper.countByMap(mp);
            mp.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(mp);
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

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
