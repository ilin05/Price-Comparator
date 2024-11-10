package com.pricecomparator.mapper;

import com.pricecomparator.entities.Product;
import com.pricecomparator.entities.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdministratorMapper {

    // 根据用户名查询用户
    @Select("SELECT id FROM user WHERE name = #{userName}")
    public User getUserInfo(String userName);
}
