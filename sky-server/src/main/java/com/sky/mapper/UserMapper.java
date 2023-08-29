package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * ClassName:UserMapper
 * Package:com.sky.mapper
 * Description:
 *
 * @Date:2023/8/25 11:44
 * @Author:QiTao
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据Id获取当前用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);
}
