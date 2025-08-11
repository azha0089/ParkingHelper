package com.example.infomanagesystem.entity.dto;

import lombok.Data;

@Data
public class UserDTO { //通用类 来存储前端传过来的数据

    private String username;
    private String password;

}
