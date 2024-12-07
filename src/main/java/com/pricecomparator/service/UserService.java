package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.utils.ApiResult;
import org.checkerframework.checker.units.qual.A;

import javax.mail.MessagingException;
import java.io.IOException;

public interface UserService {

    // 获取搜索记录，用于给予提示
    ApiResult getSearchRecords(Integer id);

    // 创建用户
    ApiResult openAccount(User user, String verifyCode, String sha256Code);

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

    // 检查收藏的商品是否降价
    ApiResult checkFavoriteProductsPrice(String email) throws IOException, InterruptedException;

    // 获取商品价格历史
    ApiResult getProductPriceHistory(String productId) throws IOException;

    // 验证邮箱
    ApiResult checkEmail(String email) throws MessagingException;
}
