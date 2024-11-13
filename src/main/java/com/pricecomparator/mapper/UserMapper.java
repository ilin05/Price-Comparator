package com.pricecomparator.mapper;

import com.pricecomparator.entities.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 创建用户
    @Insert("INSERT INTO users(username, password, email) values (#{userName}, #{password}, #{email})")
    public int openAccount(String userName, String password, String email);
    //public int openAccount(User user);

    // 根据邮箱获取用户信息
    @Select("select * from users where email = #{email}")
    public User getUserByEmail(String email);

    // 检查用户名唯一性
    @Select("select count(*) from users where username=#{name}")
    public int checkUserName(String name);

    // 检查邮箱唯一性
    @Select("select count(*) from users where email=#{email}")
    public int checkEmail(String email);

    // 匹配账户密码
    @Select("select count(*) from users where email=#{email} and password=#{password}")
    public int judgePassword(String email, String password);

    // 修改密码
    @Update("update users set password=#{newPassword} where email=#{email}")
    public int updatePassword(String newPassword, String email);

    // 修改用户名
    @Update("update users set username=#{newName} where email=#{email}")
    public int updateUserName(String email, String newName);

    // 注销账号
    @Delete("delete from users where email=#{email}")
    public int deleteUser(String email);

    // 查询搜索记录
    @Select("select * from product natural join search_records where user_id=#{id}")
    public List<Product> searchProduct(Integer id);

    // 查找是否已经添加到收藏夹
    @Select("select count(*) from favorites where email = #{email} and product_id = #{productId}")
    public int searchInFavorite(String email, String productId);

    // 添加到收藏夹
    @Insert("insert into favorites(email, product_id) values(#{email}, #{productId})")
    public int addToFavorites(String email, String productId);

    // 取消收藏
    @Delete("delete from favorites where email = #{email} and product_id=#{productId}")
    public int deleteFromFavorites(String email, String productId);

    // 根据邮箱获取收藏夹内容
    @Select("select * from products where id in (select product_id from favorites where email = #{email})")
    public List<Product> getFavorites(String email);

    // 存储商品信息
    @Insert("insert into products(id, name, category, specification, barcode, image_url, platform, link, price) values(#{id}, #{name}, #{category}, #{specification}, #{barcodeUrl}, #{imageUrl}, #{platform}, #{link}, #{price})")
    public int addProduct(Product product);

    // 检查商品是否已在库中
    @Select("select count(*) from products where id = #{productId}")
    public int getProductCount(String productId);

    // 搜索库中所有商品
    @Select("select * from products")
    public List<Product> getAllProducts();

    // 根据商品id获取商品链接
    @Select("select link from products where id = #{productId}")
    public String getProductLink(String productId);

    // 插入历史价格表
    @Insert("insert into prices(product_id, price) values(#{productId}, #{price})")
    public int addPrice(String productId, double price);

    // 如果价格低于历史最低价，更新历史最低价
    @Update("update products set price=#{price} where id=#{productId}")
    public int updatePrice(String productId, double price);

    // 获取商品价格历史
    @Select("select price from prices where product_id = #{productId}")
    public List<Double> getPriceHistory(String productId);

    // 获取商品价格历史
    @Select("select checked_at from prices where product_id = #{productId}")
    public List<String> getCheckedAt(String productId);
}
