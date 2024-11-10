package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.utils.ApiResult;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;

public interface UserService {

    // 获取搜索记录，用于给予提示
    ApiResult getSearchRecords(Integer id);

    // 创建用户
    ApiResult openAccount(User user);

    // 注销账号
    ApiResult deleteAccount(User user);

    // 修改密码
    ApiResult modifyPassword(String email, String oldPassword, String newPassword);

    // 修改用户名
    ApiResult modifyUserName(String email, String password, String newUserName);

    // 搜索商品
    ApiResult searchProducts(String productName) throws IOException;

    // 获取收藏列表
    ApiResult getUserFavorites(String email);

    // 收藏商品
    ApiResult addFavorites(String email, String productId);

    // 取消收藏商品
    ApiResult deleteFavorites(String email, String productId);

    // 用户登录
    ApiResult userLogin(String email, String password);

    // 查阅淘宝商品
    ApiResult searchTaobao(String searchProductName) throws IOException, InterruptedException;

    // 查阅唯品会商品
    ApiResult searchVip(String searchProductName) throws IOException;

    // 查阅苏宁易购商品
    ApiResult searchSuning(String searchProductName) throws IOException;

    // 查阅淘宝商品详情页
    ApiResult searchTaobaoDetail(String productId) throws IOException;

    // 查阅唯品会商品详情页
    ApiResult searchVipDetail(String productId) throws IOException, InterruptedException;

    // 查阅苏宁易购商品详情页
    ApiResult searchSuningDetail(String productId) throws IOException;

    // 检查收藏的商品是否降价
    ApiResult checkFavoriteProductsPrice(String email) throws IOException, InterruptedException;
}
