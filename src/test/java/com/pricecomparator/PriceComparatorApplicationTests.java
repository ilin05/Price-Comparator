package com.pricecomparator;

import com.pricecomparator.mapper.UserMapper;
import org.apache.xmlbeans.impl.piccolo.io.IllegalCharException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.springframework.boot.test.context.SpringBootTest;
import com.pricecomparator.service.ProductScrapter;
import com.pricecomparator.entities.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class PriceComparatorApplicationTests {
    static EdgeOptions options = new EdgeOptions();

    @Test
    void testOpenEdge(){
        options.addArguments("--remote-allow-origins=*");
        EdgeDriver driver = new EdgeDriver(options);
        driver.get("https://www.baidu.com");
    }

    @Test
    void testGetJDDetailedInfo() throws IOException, InterruptedException {
        String productId = "100011167249";
        String url = "https://item.jd.com/100021308473.html";
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        options.addArguments("--headless=old"); // 无头模式
        options.addArguments("--remote-allow-origins=*");
        WebDriver driver = new EdgeDriver(options);
        driver.get(url);
        Cookie cookie = new Cookie.Builder("thor", "0DE2F0D75DEF13A5510E9B2F30D5B5EF1E357066964ECDA9FDC6AC3824A6DFD979AD5F51119B20C0F15FFCB6C640C4EDF6D607DE165772998E6064CFBC8EFE2EAD7C50EDE99261DAAF6176A6628500803BD7B9CDC44A500F1D86631CCD6DD04376AEE81AA7F55600A8E990F58F35BBF6D9F6159975ED72D617B88E2061D2303C5ACF2472262ABB149C1C09AB576353A79039A922E4A37493741D5C00BF0A6B2B")
                .domain("jd.com")
                .path("/")
                .isSecure(true)
                .build();
        driver.manage().addCookie(cookie);
        Thread.sleep(5000); //等页面加载完成
        String pageSource = driver.getPageSource();
        driver.quit();
        Document document = Jsoup.parse(pageSource);
        //Document document = Jsoup.connect(url).cookies(cookies).get();
        //Elements elements = document.select("div.sku-name");
        //String description = elements.first().text();
        System.out.println(document);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testJDProductScrapter() throws IOException {
        String productName = "ipad";
        List<Product> productList = new ArrayList<Product>();
        String url = "https://search.jd.com/Search?keyword=" + URLEncoder.encode(productName, StandardCharsets.UTF_8);
        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put("thor", "0DE2F0D75DEF13A5510E9B2F30D5B5EF1E357066964ECDA9FDC6AC3824A6DFD979AD5F51119B20C0F15FFCB6C640C4EDF6D607DE165772998E6064CFBC8EFE2EAD7C50EDE99261DAAF6176A6628500803BD7B9CDC44A500F1D86631CCD6DD04376AEE81AA7F55600A8E990F58F35BBF6D9F6159975ED72D617B88E2061D2303C5ACF2472262ABB149C1C09AB576353A79039A922E4A37493741D5C00BF0A6B2B");
        // 解析网页, document就代表网页界面
        Document document = Jsoup.connect(url).cookies(cookies).get();
        // System.out.println(document);
        // 通过class获取ul标签
        Elements ul = document.getElementsByClass("gl-warp clearfix");
        // 获取ul标签下的所有li标签
        Elements liList = ul.select("li");
        //System.out.println(liList);
        for(Element element : liList){
            if(element.getElementsByClass("gl-item").size() == 0){
                continue;
            }
            Product product = new Product();
            //System.out.println("-------------------");
            String pictUrl = element.getElementsByTag("img").first().attr("data-lazy-img");
            // String price = element.getElementsByClass("p-price").first().text();
            String price = element.select("i[data-price]").first().attr("data-price");
            String shopName = element.getElementsByClass("p-shop").first().text();
            String productId = element.getElementsByClass("gl-item").first().attr("data-sku");
            String description = element.getElementsByTag("em").last().text();

            product.setId(productId);
            product.setName(productName);
            product.setPrice(Double.parseDouble(price));
            product.setSpecification(description);
            product.setImageUrl("https:" + pictUrl);
            product.setLink("https://item.jd.com/" + productId + ".html");
            product.setPlatform("京东");

            productList.add(product);
            System.out.println(product);
        }
    }

    @Test
    void testTbProductDetailedInfo() throws InterruptedException {
        String productId = "100011167249";
        //String url = "https://item.taobao.com/item.htm?id=" + productId;
        String url = "https://a.m.taobao.com/i709371329234.htm?priceTId=215041cd17312179609986085edbde&utparam=%7B%22aplus_abtest%22%3A%22fcb8592a5c6c27f05a9b2ae089cfdbc1%22%7D&&xxc=ad_ztc";
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        options.addArguments("--headless=old"); // 无头模式
        WebDriver driver = new EdgeDriver(options);
        driver.get(url);
        //Thread.sleep(5000); //等页面加载完成
        String pageSource = driver.getPageSource();
        driver.quit();
        Document document = Jsoup.parse(pageSource);
        //System.out.println(document);
        //String price = document.select("div[class^=highlightPrice--]").select("span[class^=text--]").text();
        String price = document.select("div[class^=purchasePanel--]").select("span[class^=text--]").text();
        System.out.println("price: " + price + "元");
//        Elements elements = document.select("div.tb-main-title");
//        String description = elements.first().text();
//        System.out.println(description);
    }

    @Test
    void testTbCrawler() throws InterruptedException {
        String searchProductName = "手机";
        List<Product> productList = new ArrayList<Product>();
        String url = "https://s.taobao.com/search?q=" + searchProductName;
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        options.addArguments("--headless=old"); // 无头模式
        WebDriver driver = new EdgeDriver(options);
        driver.get(url);
        Thread.sleep(5000); //等页面加载完成
        String pageSource = driver.getPageSource();
        driver.quit();
        Document document = Jsoup.parse(pageSource);
        //System.out.println(document);
        Elements goodsItems = document.getElementsByClass("tbpc-col search-content-col tbpc-col-lg-15 tbpc-col-xl-15 tbpc-col-xxl-12 tbpc-col tbpc-col-horizon-8 search-content-col tbpc-col-lg-15 tbpc-col-xl-15 tbpc-col-xxl-12");
        for(Element goodsItem : goodsItems){
            String description = goodsItem.select("div.title--qJ7Xg_90").first().select("span").text();
            String price = goodsItem.select("span[class^=priceInt--]").text();
            String priceDec = goodsItem.select("span[class^=priceFloat--]").text();
            if(priceDec.length() > 0){
                price = price + "." + priceDec;
            }
            String productId = goodsItem.getElementsByClass("ww-light ww-small").attr("data-item");
            String pictUrl = goodsItem.select("img[class^=mainPic--]").first().attr("src");
            //String pictUrl = goodsItem.select("img").first().attr("src");
            String productLink = goodsItem.select("a").first().attr("href");
            //System.out.println(goodsItem);
            System.out.println("商品id: " + productId);
            System.out.println("商品名称: " + description);
            System.out.println("价格: " + price);
            System.out.println("图片路径: " + pictUrl);
            System.out.println("商品链接: " + productLink);
            System.out.println("--------------------------");
        }
    }

    @Test
    void testRealTbCrawler() throws InterruptedException {
        String searchProductName = "电脑";
        List<Product> productList = new ArrayList<Product>();
        String url = "https://uland.taobao.com/sem/tbsearch?q=" + searchProductName;
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        //options.addArguments("--headless=old"); // 无头模式
        WebDriver driver = new EdgeDriver(options);
        driver.get(url);
        //Thread.sleep(5000); //等页面加载完成
        String pageSource = driver.getPageSource();
        driver.quit();
        Document document = Jsoup.parse(pageSource);
        //System.out.println(document);
        Elements goodsItems = document.select("a[class^=Card--doubleCardWrapper--]");
        for(Element goodsItem : goodsItems){
            if(goodsItem.select("div[class^=Title--title--]").select("span").text().length() == 0){
                continue;
            }
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
            //String pictUrl = goodsItem.select("img").first().attr("src");
            //System.out.println(goodsItem);
            System.out.println("商品id: " + productId);
            System.out.println("商品名称: " + description);
            System.out.println("价格: " + price);
            System.out.println("图片路径: " + pictUrl);
            System.out.println("商品链接: " + productLink);
            System.out.println("--------------------------");
        }
    }

    @Test
    void testSnProductDetailedInfo() throws InterruptedException {
        String productId = "100011167249";
        String url = "https://product.suning.com/0000000000/12435768901.html";
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
        System.out.println("price: " + price);
    }

    @Test
    void testSnCrawler() throws IOException {
        String searchProductName = "手机";
        List<Product> productList = new ArrayList<Product>();
        String url = "https://search.suning.com/" + URLEncoder.encode(searchProductName, StandardCharsets.UTF_8) + "/";
        try{
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
            for(Element goodsItem : goodsItems){
                String pictUrl = goodsItem.getElementsByTag("img").first().attr("src");
                String price = goodsItem.getElementsByClass("def-price").first().text().replace("¥", "").trim();
                //String price = element.select("i[data-price]").first().attr("data-price");
                //String shopName = element.getElementsByClass("p-shop").first().text();
                String productId = goodsItem.select("a.sellPoint").first().attr("sa-data").split("'prdid':'")[1].split("'")[0];
                String description = goodsItem.getElementsByClass("title-selling-point").select("a").first().text();
                // String description = goodsItem.select("a").first().text();
                String productLink = goodsItem.select("a").first().attr("href");
                System.out.println("商品id: " + productId);
                System.out.println("商品名称: " + description);
                System.out.println("价格: " + price);
                System.out.println("图片路径: " + pictUrl);
                System.out.println("商品链接: " + productLink);
                System.out.println("--------------------------");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void testVipProductDetailedInfo() throws InterruptedException {
        String productId = "100011167249";
        String url = "https://detail.vip.com/detail-1710756431-6920797323155458959.html";
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
        System.out.println("price: " + price);
    }

    @Test
    void testWphCrawler() throws IOException {
        String searchProductName = "手机";
        String url = "https://category.vip.com/suggest.php?keyword=" + searchProductName;
        //Document document = Jsoup.connect(url).get();
        //System.out.println(document);
        try{
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
//            options.addArguments("--headless=old"); // 无头模式
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
                // 提取市场价
                // String marketPrice = goodsItem.select(".c-goods-item__market-price").text();
                // 提取折扣
                // String discount = goodsItem.select(".c-goods-item__discount").text();
                // 提取图片路径
                String imageUrl = goodsItem.select(".c-goods-item__img img").attr("src");
                // 输出商品信息
                System.out.println("商品id: " + productId);
                System.out.println("商品名称: " + productName);
                System.out.println("价格: " + price);
                System.out.println("图片路径: " + imageUrl);
                System.out.println("商品链接: " + productLink);
                System.out.println("--------------------------");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
