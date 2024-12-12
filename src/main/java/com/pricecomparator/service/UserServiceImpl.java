package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.mapper.UserMapper;
import com.pricecomparator.utils.ApiResult;
import com.pricecomparator.utils.ProductSearcher;
import com.pricecomparator.utils.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.openqa.selenium.edge.EdgeOptions;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    public ApiResult openAccount(User user, String verifyCode, String sha256Code) {
        try{
            if(!HashUtils.sha256Hash(verifyCode).equals(sha256Code)){
                return ApiResult.failure("验证码错误");
            }
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
            userMapper.openAccount(userName, HashUtils.sha256Hash(user.getPassword()), email);
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
            String hashedOldPassword = HashUtils.sha256Hash(oldPassword);
            String hashedNewPassword = HashUtils.sha256Hash(newPassword);
            int count = userMapper.judgePassword(email, hashedOldPassword);
            if(count != 1){
                return ApiResult.failure("邮箱或密码错误");
            }
            userMapper.updatePassword(hashedNewPassword, email);
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
            String hashedPassword = HashUtils.sha256Hash(password);
            int count = userMapper.judgePassword(email, hashedPassword);
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
        try {
            // List<Product> productList = ProductSearcher.searchProductInOneThread(productName);
            Process process = Runtime.getRuntime().exec("python.exe src/main/python/tellCategory.py \"" + productName + "\"");
            InputStream inputStream = process.getInputStream();
            process.waitFor();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            String category = outputStream.toString("GBK");

            List<Product> productList = ProductSearcher.searchTogether(productName);
            Collections.shuffle(productList);
            for(Product product : productList){
                product.setCategory(category);
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
            int count = userMapper.judgePassword(email, HashUtils.sha256Hash(password));
            String userName = userMapper.getUserName(email);
            if(count != 1){
                return ApiResult.failure("账户名或密码错误！");
            }else{
                HashMap<String, String> user = new HashMap<String, String>();
                user.put("email", email);
                user.put("userName", userName);
                // return ApiResult.success(email);
                return ApiResult.success(user);
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
//            for(Product product : products){
//                System.out.println(product);
//            }
//            System.out.println("--------------------");

            List<Product> checkedProducts = ProductSearcher.checkFavoriteProductsPrice(products);
            for(Product product : checkedProducts){
                userMapper.updatePrice(product.getId(), product.getPrice());
                userMapper.addPrice(product.getId(), product.getPrice());
                Double previousPrice = product.getPrice();
                if(previousPrice > product.getPrice()){
                    priceChangedProducts.add(product);
                }
            }

//            for(Product product : products){
//                String platform = product.getPlatform();
//                String productId = product.getId();
//                //String url = userMapper.getProductLink(productId);
//                String url = product.getLink();
//                System.out.println("url: " + url);
//                System.out.println("platform: " + platform);
//                Double price = 0.0;
//                if(platform.equals("淘宝")){
//                    price = ProductSearcher.getTaobaoPrice(url);
//                }else if(platform.equals("唯品会")){
//                    price = ProductSearcher.getVipPrice(url);
//                }else if(platform.equals("苏宁易购")){
//                    price = ProductSearcher.getSuningPrice(url);
//                }else if(platform.equals("小米有品")){
//                    price = ProductSearcher.getXMPrice(url);
//                }
//                userMapper.addPrice(productId, price);
//                userMapper.updatePrice(productId, price);
//                Double previousPrice = product.getPrice();
//                if(previousPrice > price){
//                    product.setPreviousPrice(previousPrice);
//                    product.setPrice(price);
//                    priceChangedProducts.add(product);
//                }
//            }
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

    @Override
    public ApiResult checkEmail(String email) throws MessagingException {
        try{
            String username = "2105578728@qq.com";
            String password = "xqknugfuqqykcdeb";

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.qq.com");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            session.setDebug(true);

            // 生成六位随机数
            StringBuilder code = new StringBuilder();
            for(int i = 0; i < 6; i++){
                code.append((int) (Math.random() * 10));
            }
            System.out.println("code: " + code);
            String sha256Code = HashUtils.sha256Hash(code.toString());
            MimeMessage message = new MimeMessage(session);
            message.setSubject("Price Comparator 验证码");
            message.setText("您好！您的验证码是：" + code);
            message.setFrom(new InternetAddress("2105578728@qq.com"));
            message.setRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(email)));

            Transport.send(message);
            return ApiResult.success(sha256Code);
        }
        catch (Exception e){
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error sending email");
        }
    }

}
