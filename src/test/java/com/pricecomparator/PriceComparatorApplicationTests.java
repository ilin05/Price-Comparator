package com.pricecomparator;

import com.pricecomparator.mapper.UserMapper;
import com.pricecomparator.service.UserService;
import com.pricecomparator.utils.ApiResult;
import org.apache.xmlbeans.impl.piccolo.io.IllegalCharException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.pricecomparator.utils.ProductSearcher;
import com.pricecomparator.entities.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;

@SpringBootTest
class PriceComparatorApplicationTests {
    static EdgeOptions options = new EdgeOptions();
    static ProductSearcher productSearcher = new ProductSearcher();
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

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
            Double price = productSearcher.getTaobaoPrice(url);
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
            Double price = productSearcher.getSuningPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetVipProductPrice(){
        String url = "https://detail.vip.com/detail-1711843802-6921026343352098970.html";
        try {
            Double price = productSearcher.getVipPrice(url);
            System.out.println("商品价格: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSearchTBProduct() throws IOException, InterruptedException {
        String productName = "大米";
        List<Product> productList = productSearcher.searchTaobao(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchSuningProduct() throws InterruptedException {
        String productName = "大米";
        List<Product> productList = productSearcher.searchSuning(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchVipProduct() throws IOException {
        String productName = "手机";
        List<Product> productList = productSearcher.searchVip(productName);
        for(Product product : productList){
            System.out.println(product);
        }
    }

    @Test
    void testSearchTogetherOneThread(){
        String productName = "电脑";
        List<Product> productList = new ArrayList<>();
        try {
            List<Product> taobaoList = productSearcher.searchTaobao(productName);
            List<Product> suningList = productSearcher.searchSuning(productName);
            List<Product> vipList = productSearcher.searchVip(productName);
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
        String productName = "电脑";
        List<Product> productList = new ArrayList<>();

        try {
            CompletableFuture<List<Product>> taobaoFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return productSearcher.searchTaobao(productName);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<List<Product>> suningFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return productSearcher.searchSuning(productName);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<List<Product>> vipFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return productSearcher.searchVip(productName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(taobaoFuture, suningFuture, vipFuture);

            allFutures.thenApply(v -> {
                try {
                    productList.addAll(taobaoFuture.get());
                    productList.addAll(suningFuture.get());
                    productList.addAll(vipFuture.get());
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
}
