package com.example.infomanagesystem.controller;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

import com.example.infomanagesystem.entity.User;
import com.example.infomanagesystem.entity.dto.UserDTO;
import com.example.infomanagesystem.result.R;

import com.example.infomanagesystem.service.UserService;
import com.example.infomanagesystem.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//登录接口
@RestController
@CrossOrigin
public class LoginAndRegisterController {
    @Autowired
    private UserService userService;
    


    @PostMapping("/login")
    public R login(@RequestBody UserDTO userDTO){

          String username=userDTO.getUsername();
          String password= MD5Util.encrypt(userDTO.getPassword());
           User user=userService.login(username,password);
              if(user!=null){//用户存在

                  StpUtil.login(user.getId());

                  SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
                  System.out.println("登录成功得到的token 是"+tokenInfo);
                  return new R(true,200,"student",tokenInfo);
              }
              else{
                  return new R(false,404,"学生用户不存在!请检查用户名或者密码是否正确");
              }

    }
    @PostMapping("/register")
    public R register(@RequestBody UserDTO userDTO){

        String username=userDTO.getUsername();
        String password=MD5Util.encrypt(userDTO.getPassword());


        User user=new User(); //状态默认为1 账号可以使用
        user.setUsername(username);
        user.setPassword(password);

            if(userService.register(user)){ //true
                return new R(true,201,"学生用户添加成功");
            }
            else{ //false
                return new R(false,400,"该用户已存在");
            }
    }

}