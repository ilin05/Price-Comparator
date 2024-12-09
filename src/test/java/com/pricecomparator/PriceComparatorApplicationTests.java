package com.pricecomparator;

import com.pricecomparator.mapper.UserMapper;
import com.pricecomparator.service.UserService;
import com.pricecomparator.utils.*;
import org.apache.xmlbeans.impl.piccolo.io.IllegalCharException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.pricecomparator.entities.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.awaitility.Awaitility.await;

@SpringBootTest
class PriceComparatorApplicationTests {
    static EdgeOptions options = new EdgeOptions();

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    static ProductSearcher productSearcher = new ProductSearcher();

    @Test
    void testXMProductPrice() throws IOException {
        String url = "https://www.xiaomiyoupin.com/detail?gid=162729";
        Double price = ProductSearcher.getXMPrice(url);
        System.out.println("商品价格: " + price);
    }

    @Test
    void testXMCrawler() throws IOException {
        List<Product> productList = ProductSearcher.searchXiaoMiYouPin("手机");
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSendMail() throws MessagingException {
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

        MimeMessage message = new MimeMessage(session);
        message.setSubject("测试邮件");
        message.setText("这是一封测试邮件");
        message.setFrom(new InternetAddress("2105578728@qq.com"));
        message.setRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress("zhanglin20050530@163.com")));

        Transport.send(message);
    }

    @Test
    void testCheckPriceChange() throws IOException, InterruptedException {
        String email = "zhanglin20050530@163.com";
        ApiResult result = userService.checkFavoriteProductsPrice(email);
        List<Product> productList = (List<Product>) result.payload;
        for (Product product : productList) {
            System.out.println(product);
        }
        //System.out.println(result.payload);
    }

    @Test
    void testFindFavoriteProducts(){
        String email = "zhanglin20050530@163.com";
        List<Product> productList = userMapper.getFavorites(email);
        for (Product product : productList) {
            System.out.println(product);
        }
    }

    @Test
    void testOpenEdge(){
        options.addArguments("--remote-allow-origins=*");
        EdgeDriver driver = new EdgeDriver(options);
        driver.get("https://www.baidu.com");
    }

    @Test
    void testGetTaoBaoProductPrice(){
        String url = "https://a.m.taobao.com/i842805637189.htm";
        try {
            Double price = ProductSearcher.getTaobaoPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetSuningProductPrice(){
        String url = "https://product.suning.com/0010347623/12414162648.html";
        System.out.println("url byte数：" + url.length());
        try {
            Double price = ProductSearcher.getSuningPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetVipProductPrice(){
        String url = "https://detail.vip.com/detail-1711843802-6921026343352098970.html";
        try {
            Double price = ProductSearcher.getVipPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetJDProductPrice(){
        String url = "https://item.jd.com/11523185865.html";
        try {
            Double price = ProductSearcher.getJDPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSearchTBProduct() throws IOException, InterruptedException {
        String productName = "充电宝";
        List<Product> productList = ProductSearcher.searchTaobao(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchSuningProduct() throws InterruptedException {
        String productName = "大米";
        List<Product> productList = ProductSearcher.searchSuning(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchVipProduct() throws IOException {
        String productName = "手机";
        List<Product> productList = ProductSearcher.searchVip(productName);
        // List<Product> productList = VipSeacher.searchVip(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchTogetherOneThread(){
        String productName = "电脑";
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
                System.out.println(product);
            }
            System.out.println("共搜索到" + productList.size() + "个商品");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSearchTogether() {
        String productName = "鸡蛋";
        List<Product> productList = new ArrayList<>();

        try {
            CompletableFuture<List<Product>> taobaoFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return ProductSearcher.searchTaobao(productName);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<List<Product>> suningFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return ProductSearcher.searchSuning(productName);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<List<Product>> vipFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return ProductSearcher.searchVip(productName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<List<Product>> xmFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return ProductSearcher.searchXiaoMiYouPin(productName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(taobaoFuture, suningFuture, vipFuture, xmFuture);

            allFutures.thenApply(v -> {
                try {
                    productList.addAll(taobaoFuture.get());
                    productList.addAll(suningFuture.get());
                    productList.addAll(vipFuture.get());
                    productList.addAll(xmFuture.get());
                    System.out.println("共搜索到" + productList.size() + "个商品");
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }).join();

            for (Product product : productList) {
                System.out.println(product);
            }
            System.out.println("共搜索到" + productList.size() + "个商品");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testProductSeacherTogether(){
        List<Product> productList = ProductSearcher.searchTogether("电脑");
        for(Product product : productList){
            System.out.println(product);
        }
        System.out.println("共搜索到" + productList.size() + "个商品");
    }

    @Test
    void testCheckFavoriteProductsPrice() throws IOException, InterruptedException {
        String email = "zhanglin20050530@163.com";
        ApiResult result = userService.checkFavoriteProductsPrice(email);
    }
}
