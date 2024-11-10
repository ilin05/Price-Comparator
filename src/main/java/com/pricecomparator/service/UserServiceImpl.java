package com.pricecomparator.service;

import com.pricecomparator.entities.*;
import com.pricecomparator.service.*;
import com.pricecomparator.mapper.UserMapper;
import com.pricecomparator.utils.ApiResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    static EdgeOptions options = new EdgeOptions();
    private final UserMapper userMapper;
    private final ProductScrapter productScrapter;

    public UserServiceImpl(UserMapper userMapper, ProductScrapter productScrapter) {
        this.userMapper = userMapper;
        this.productScrapter = productScrapter;
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
    public ApiResult searchProducts(String productName) throws IOException {
//        List<Product> products = productScrapter.getProduct(productName);
//        for(Product product : products){
//            if(userMapper.getProductCount(product.getId()) == 0){
//                System.out.println(product);
//                userMapper.addProduct(product);
//            }
//        }
        List<Product> products = userMapper.getAllProducts();
        return ApiResult.success(products);
        //return products;
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
    public ApiResult searchTaobao(String searchProductName) throws InterruptedException {
        try {
            List<Product> productList = new ArrayList<Product>();
            String url = "https://uland.taobao.com/sem/tbsearch?q=" + searchProductName;
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            //System.out.println(document);
            Elements goodsItems = document.select("a[class^=Card--doubleCardWrapper--]");
            for(Element goodsItem : goodsItems){
                if(goodsItem.select("div[class^=Title--title--]").select("span").text().length() == 0){
                    continue;
                }
                Product product = new Product();
                //System.out.println(goodsItem.select("div[class^=Title--title--]").select("span").text());
                //System.out.println(goodsItem);
                String description = goodsItem.select("div[class^=Title--title--]").select("span").text();
                String price = goodsItem.select("span[class^=Price--priceInt--]").text();
                String priceDec = goodsItem.select("span[class^=Price--priceFloat--]").text();
                if(priceDec.length() > 0){
                    price = price + priceDec;
                }
                String productLink = goodsItem.attr("href");
                String pattern = "i(\\d+)\\.htm";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(productLink);
                String productId = "";
                if(m.find()){
                    System.out.println("Found value: " + m.group(1));
                    productId = m.group(1);
                }

                String pictUrl = goodsItem.select("img[class^=MainPic--mainPicWrapper--]").select("img").attr("src");
                product.setPlatform("淘宝");
                product.setId(productId);
                product.setName(description);
                product.setPrice(Double.parseDouble(price));
                product.setLink(productLink);
                product.setImageUrl(pictUrl);
                productList.add(product);
                //String pictUrl = goodsItem.select("img").first().attr("src");
                //System.out.println(goodsItem);
//            System.out.println("商品id: " + productId);
//            System.out.println("商品名称: " + description);
//            System.out.println("价格: " + price);
//            System.out.println("图片路径: " + pictUrl);
//            System.out.println("商品链接: " + productLink);
//            System.out.println("--------------------------");
            }
            //return null;
            return ApiResult.success(productList);
        } catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Taobao");
        }
    }

    @Override
    public ApiResult searchSuning(String searchProductName) throws IOException {
        try {
            List<Product> productList = new ArrayList<Product>();
            String url = "https://search.suning.com/" + URLEncoder.encode(searchProductName, StandardCharsets.UTF_8) + "/";
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            //System.out.println(document);
            Elements ul = document.getElementsByClass("general clearfix");
            // 获取ul标签下的所有li标签
            Elements goodsItems = ul.select("li");
            //System.out.println(goodsItems);
            for(Element goodsItem : goodsItems) {
                String pictUrl = goodsItem.getElementsByTag("img").first().attr("src");
                String price = goodsItem.getElementsByClass("def-price").first().text().replace("¥", "").trim();
                //String price = element.select("i[data-price]").first().attr("data-price");
                //String shopName = element.getElementsByClass("p-shop").first().text();
                String productId = goodsItem.select("a.sellPoint").first().attr("sa-data").split("'prdid':'")[1].split("'")[0];
                String description = goodsItem.getElementsByClass("title-selling-point").select("a").first().text();
                String productLink = goodsItem.select("a").first().attr("href");
                Product product = new Product();
                product.setPlatform("苏宁易购");
                product.setId(productId);
                product.setName(description);
                product.setPrice(Double.parseDouble(price));
                product.setLink(productLink);
                product.setImageUrl(pictUrl);
                productList.add(product);
                //                System.out.println("商品id: " + productId);
//                System.out.println("商品名称: " + description);
//                System.out.println("价格: " + price);
//                System.out.println("图片路径: " + pictUrl);
//                System.out.println("商品链接: " + productLink);
//                System.out.println("--------------------------");
            }
            return ApiResult.success(productList);
        } catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Vip");
        }
    }

    @Override
    public ApiResult searchTaobaoDetail(String productId) throws IOException {
        try {
            String url = userMapper.getProductLink(productId);
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            //Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            //System.out.println(document);
            String price = document.select("div[class^=highlightPrice--]").select("span[class^=text--]").text();

            userMapper.addPrice(productId, Double.parseDouble(price));
            //System.out.println("price: " + price + "元");
            //return null;
            return ApiResult.success(price);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Taobao");
        }
    }

    @Override
    public ApiResult searchVipDetail(String productId) throws InterruptedException {
        try {
            String url = userMapper.getProductLink(productId);
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            String price = document.getElementById("J-topBar-sell-price").text();
            //String price = document.select("cart2Price").text();
            //System.out.println("price: " + price);
            userMapper.addPrice(productId, Double.parseDouble(price));
            return ApiResult.success(price);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Vip");
        }
    }

    @Override
    public ApiResult searchSuningDetail(String productId) throws IOException {
        try {
            String url = userMapper.getProductLink(productId);
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            String price = document.getElementById("cart2Price").text();
            //String price = document.select("cart2Price").text();
            //System.out.println("price: " + price);
            userMapper.addPrice(productId, Double.parseDouble(price));
            return ApiResult.success(price);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Vip");
        }
    }

    @Override
    public ApiResult checkFavoriteProductsPrice(String email) throws IOException, InterruptedException {
        try {
            List<Product> products = userMapper.getFavorites(email);
            List<Product> priceChangedProducts = new ArrayList<Product>();
            for(Product product : products){
                String platform = product.getPlatform();
                String productId = product.getId();
                String price = "";
                if(platform.equals("淘宝")){
                    price = searchTaobaoDetail(productId).getPayload().toString();
                }else if(platform.equals("唯品会")){
                    price = searchVipDetail(productId).getPayload().toString();
                }else if(platform.equals("苏宁易购")){
                    price = searchSuningDetail(productId).getPayload().toString();
                }
                if(product.getPrice() > Double.parseDouble(price)){
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
    public ApiResult searchVip(String searchProductName) throws IOException {
        try {
            List<Product> productList = new ArrayList<Product>();
            String url = "https://category.vip.com/suggest.php?keyword=" + searchProductName;
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            //System.out.println(document);
            Elements goodsItems = document.select(".c-goods-item");
            //System.out.println(goodsItems);
            for(Element goodsItem : goodsItems){
                // 提取商品名称
                String productName = goodsItem.select(".c-goods-item__name").text();
                // 提取特卖价
                String salePrice = goodsItem.select(".c-goods-item__sale-price").text();
                String price = salePrice.replaceAll("[^0-9.]", "");
                // 提取商品链接
                String productLink = goodsItem.select("a").first().attr("href");
                // 提取商品id
                String productId = goodsItem.attr("data-product-id");
                String imageUrl = goodsItem.select(".c-goods-item__img img").attr("src");
                Product product = new Product();
                product.setPlatform("唯品会");
                product.setId(productId);
                product.setName(productName);
                product.setPrice(Double.parseDouble(price));
                product.setLink(productLink);
                product.setImageUrl(imageUrl);
                productList.add(product);
                // 输出商品信息
//                System.out.println("商品id: " + productId);
//                System.out.println("商品名称: " + productName);
//                System.out.println("价格: " + price);
//                System.out.println("图片路径: " + imageUrl);
//                System.out.println("商品链接: " + productLink);
//                System.out.println("--------------------------");
            }
            return ApiResult.success(productList);
        }
        catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error searching Vip");
        }
    }

}
