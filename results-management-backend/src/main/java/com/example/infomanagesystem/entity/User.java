package com.example.infomanagesystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

//学生实体类
@Data
@TableName("user") //绑定数据库的表名
public class User {

    private Long id;


    private String username; //用户名


    private String password; //密码

}
