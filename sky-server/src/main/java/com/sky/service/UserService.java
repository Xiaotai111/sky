package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * ClassName:UserService
 * Package:com.sky.service
 * Description:
 *
 * @Date:2023/8/25 11:22
 * @Author:QiTao
 */
public interface UserService {

    /**
     * 微信登陆
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
