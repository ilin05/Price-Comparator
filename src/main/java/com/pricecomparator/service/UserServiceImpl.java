package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.service.*;
import com.pricecomparator.mapper.UserMapper;
import com.pricecomparator.utils.ApiResult;
import com.pricecomparator.utils.ProductSearcher;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    static EdgeOptions options = new EdgeOptions();
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public ApiResult getSearchRecords(Integer id) {
        return null;
    }

    @Override
    @Transactional
    public ApiResult openAccount(User user) {
        try{
            String email = user.getEmail();
            int count = userMapper.checkEmail(email);
            if(count > 0){
                return ApiResult.failure("Email already in use");
                //throw new RuntimeException("Email already registered");
            }
            System.out.println("hello2");
            String userName = user.getUserName();
            System.out.println(userName);
            int count2 = userMapper.checkUserName(userName);
            System.out.println(count2);
            if(count2 > 0){
                return ApiResult.failure("Username already in use");
                //throw new RuntimeException("User name already exists");
            }
            //System.out.println("count2: " + count2);

            System.out.println("hello3");
            userMapper.openAccount(userName, user.getPassword(), email);
            User newUser = userMapper.getUserByEmail(email);
            System.out.println("hello4");
            return ApiResult.success(newUser);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error opening account");
        }
        //return null;
    }

    @Override
    public ApiResult deleteAccount(User user) {
        try{
            String email = user.getEmail();
            String password = user.getPassword();
            int count = userMapper.judgePassword(email, password);
            if(count != 1){
                return ApiResult.failure("邮箱或密码错误");
            }
            userMapper.deleteUser(email);
            return ApiResult.success(null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error deleting account");
        }
    }

    @Override
    public ApiResult modifyPassword(String email, String oldPassword, String newPassword) {
        try{
            int count = userMapper.judgePassword(email, oldPassword);
            if(count != 1){
                return ApiResult.failure("邮箱或密码错误");
            }
            userMapper.updatePassword(newPassword, email);
            return ApiResult.success(null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error modify password");
        }
    }

    @Override
    @Transactional
    public ApiResult modifyUserName(String email, String password, String newUserName) {
        try{
            int count = userMapper.judgePassword(email, password);
            if(count != 1){
                return ApiResult.failure("邮箱或密码错误");
            }
            count = userMapper.checkUserName(newUserName);
            if(count > 0){
                return ApiResult.failure("Username already in use");
            }
            userMapper.updateUserName(email, newUserName);
            return ApiResult.success(null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error modify user name");
        }
    }

    @Override
    @Transactional
    public ApiResult searchProducts(String productName) throws IOException {
        List<Product> productList = new ArrayList<>();
        try {
            List<Product> taobaoList = ProductSearcher.searchTaobao(productName);
            List<Product> suningList = ProductSearcher.searchSuning(productName);
            List<Product> vipList = ProductSearcher.searchVip(productName);
            productList.addAll(taobaoList);
            productList.addAll(suningList);
            productList.addAll(vipList);
            Collections.shuffle(productList);

            for(Product product : productList){
                System.out.println("hello");
                if(userMapper.getProductCount(product.getId()) == 0){
                    userMapper.addProduct(product);
                }
                userMapper.addPrice(product.getId(), product.getPrice());
                //System.out.println(product);
            }
            System.out.println("共搜索到" + productList.size() + "个商品");
            return ApiResult.success(productList);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching products");
        }
    }

    @Override
    public ApiResult getUserFavorites(String email) {
        try{
            List<Product> products = userMapper.getFavorites(email);
            return ApiResult.success(products);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error getting user favorites");
        }
        //return null;
    }

    @Override
    @Transactional
    public ApiResult addFavorites(String email, String productId) {
        try{
            int count = userMapper.searchInFavorite(email, productId);
            if(count > 0){
                return ApiResult.failure("已收藏过该商品");
            }else{
                userMapper.addToFavorites(email, productId);
                return ApiResult.success(null);
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error adding favorites");
        }
    }

    @Override
    public ApiResult deleteFavorites(String email, String productId) {
        try{
            userMapper.deleteFromFavorites(email, productId);
            return ApiResult.success(null);
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error deleting favorites");
        }
    }

    @Override
    public ApiResult userLogin(String email, String password) {
        try{
            int count = userMapper.judgePassword(email, password);
            if(count != 1){
                return ApiResult.failure("账户名或密码错误！");
            }else{
                return ApiResult.success(email);
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error judging user login");
        }
    }


    @Override
    @Transactional
    public ApiResult checkFavoriteProductsPrice(String email) throws IOException, InterruptedException {
        try {
            System.out.println("email: " + email);
            List<Product> products = userMapper.getFavorites(email);
            List<Product> priceChangedProducts = new ArrayList<Product>();
            for(Product product : products){
                System.out.println(product);
            }
            System.out.println("--------------------");
            for(Product product : products){
                String platform = product.getPlatform();
                String productId = product.getId();
                //String url = userMapper.getProductLink(productId);
                String url = product.getLink();
                System.out.println("url: " + url);
                System.out.println("platform: " + platform);
                Double price = 0.0;
                if(platform.equals("淘宝")){
                    price = ProductSearcher.getTaobaoPrice(url);
                }else if(platform.equals("唯品会")){
                    price = ProductSearcher.getVipPrice(url);
                }else if(platform.equals("苏宁易购")){
                    price = ProductSearcher.getSuningPrice(url);
                }
                userMapper.addPrice(productId, price);
                userMapper.updatePrice(productId, price);
                if(product.getPrice() > price){
                    product.setPreviousPrice(product.getPrice());
                    product.setPrice(price);
                    priceChangedProducts.add(product);
                }
            }
            return ApiResult.success(priceChangedProducts);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Vip");
        }
    }

    @Override
    @Transactional
    public ApiResult getProductPriceHistory(String productId) throws IOException {
        try {
            System.out.println("productId: " + productId);
            List<Double> prices = userMapper.getPriceHistory(productId);
            List<String> dates = userMapper.getCheckedAt(productId);
            System.out.println(prices);
            System.out.println(dates);
            return ApiResult.success(Map.of("prices", prices, "dates", dates));
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error getting product price history");
        }
        //return null;
    }

}
