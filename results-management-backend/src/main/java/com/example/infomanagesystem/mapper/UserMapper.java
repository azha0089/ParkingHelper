package com.example.infomanagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.infomanagesystem.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select count(*) from user") //sql语句应该为双引号
    int getStudentNum();
}
