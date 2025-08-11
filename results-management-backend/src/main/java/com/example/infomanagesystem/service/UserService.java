package com.example.infomanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.infomanagesystem.entity.User;


public interface UserService extends IService<User> {


     User login(String username, String password); //登录  检测学生用户是否存在
     boolean register(User user);  //学生注册功能

}
