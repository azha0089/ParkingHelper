package com.example.infomanagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.infomanagesystem.entity.User;
import com.example.infomanagesystem.mapper.UserMapper;
import com.example.infomanagesystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//学生登录 注册

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper studentMapper;


    @Override
    public User login(String username ,String password) {
            QueryWrapper<User> q=new QueryWrapper<>();
            //寻找用户
            q.eq("username",username).eq("password",password);

        return studentMapper.selectOne(q);
    }
    @Override
    public boolean register(User student) { //注册成功  同时要判断用户是否已经存在
        String username=student.getUsername();
        QueryWrapper<User> q=new QueryWrapper<>();
        q.eq("username",username);
        if(studentMapper.selectOne(q)!=null){ //用户存在
            return false;
        }
        //增加操作 //用户不存在
        return studentMapper.insert(student) > 0;
    }

}
