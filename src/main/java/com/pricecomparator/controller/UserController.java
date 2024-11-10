package com.pricecomparator.controller;

import com.pricecomparator.entities.*;
import com.pricecomparator.service.UserService;
import com.pricecomparator.utils.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.List;

@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // 开户
    @PostMapping("/openAccount")
    public ApiResult openAccount(@RequestBody Map<String,String> newAccountInfo) {
        User user = new User();
        user.setPassword(newAccountInfo.get("password"));
        user.setEmail(newAccountInfo.get("email"));
        user.setUserName(newAccountInfo.get("userName"));
        return userService.openAccount(user);
    }
//    public ApiResult openAccount(@RequestParam User user) {
//        return userService.openAccount(user);
//    }

    // 销户
    @PostMapping("/deleteAccount")
    public ApiResult deleteAccount(@RequestBody User user) {
        return userService.deleteAccount(user);
    }

    // 修改密码
    @PostMapping("/modifyPassword")
    public ApiResult modifyPassword(@RequestBody Map<String,String> modifytPasswordInfo) {
        System.out.println(modifytPasswordInfo);
        String email = modifytPasswordInfo.get("email");
        String oldPassword = modifytPasswordInfo.get("oldPassword");
        String newPassword = modifytPasswordInfo.get("newPassword");
        return userService.modifyPassword(email, oldPassword, newPassword);
    }

    // 修改用户名
    @PostMapping("/modifyUserName")
    public ApiResult modifyUserName(@RequestBody Map<String,String> modifytUserName) {
        String email = modifytUserName.get("email");
        String password = modifytUserName.get("password");
        String newName = modifytUserName.get("newName");
        return userService.modifyUserName(email, password, newName);
    }

    // 收藏商品
    @PostMapping("/addFavorites")
    public ApiResult addFavorites(@RequestBody Map<String,String> addFavoritesInfo) {
        String email = addFavoritesInfo.get("email");
        String productId = addFavoritesInfo.get("productId");
        System.out.println(email);
        System.out.println(productId);
        return userService.addFavorites(email, productId);
    }

    //取消收藏商品
    @PostMapping("/deleteFavorites")
    public ApiResult deleteFavorites(@RequestBody Map<String,String> deleteFavoritesInfo) {
        String email = deleteFavoritesInfo.get("email");
        String productId = deleteFavoritesInfo.get("productId");
        return userService.deleteFavorites(email, productId);
    }

    // 获取账户收藏列表
    @GetMapping("/getFavorites")
    public ApiResult getUserFavorites(@RequestParam String email) {
        return userService.getUserFavorites(email);
    }

    // 搜索商品
    @GetMapping("/searchProducts")
    public ApiResult searchProducts(@RequestParam String productName) throws IOException {
        System.out.println(productName);

        return userService.searchProducts(productName);
    }

    // 用户登录
    @PostMapping("/login")
    public ApiResult userLogin(@RequestBody Map<String,String> userLoginInfo) {
        String email = userLoginInfo.get("email");
        String password = userLoginInfo.get("password");
        System.out.println(email);
        System.out.println(password);
        return userService.userLogin(email, password);
    }


}
