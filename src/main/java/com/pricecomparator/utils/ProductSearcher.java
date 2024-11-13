package com.pricecomparator.utils;
import com.pricecomparator.mapper.UserMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.pricecomparator.entities.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductSearcher {
    static EdgeOptions options = new EdgeOptions();

    static public List<Product> searchTaobao(String searchProductName) throws InterruptedException {
        System.out.println("Search Taobao");
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
                String productLink = goodsItem.attr("href");
                String pattern = "i(\\d+)\\.htm";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(productLink);
                String productId = "";
                if(m.find()){
                    System.out.println("Found value: " + m.group(1));
                    productId = m.group(1);
                }
                if(productId.length() == 0){
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

                String pictUrl = goodsItem.select("img[class^=MainPic--mainPicWrapper--]").select("img").attr("src");
                product.setPlatform("淘宝");
                product.setId("tb_" + productId);
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
            return productList;
        } catch (Exception e){
            return null;
        }
    }

    static public List<Product> searchSuning(String searchProductName) throws InterruptedException {
        System.out.println("Search Suning");
        try {
            List<Product> productList = new ArrayList<Product>();
            String url = "https://search.suning.com/" + searchProductName + "/";
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
            for (Element goodsItem : goodsItems) {
                String price = goodsItem.getElementsByClass("def-price").first().text().replace("¥", "").trim();
                if(price.length() == 0){
                    continue;
                }
                Pattern pattern = Pattern.compile("\\d+\\.\\d{2}");
                Matcher matcher = pattern.matcher(price);
                if(matcher.find()){
                    price = matcher.group();
                }
                String pictUrl = goodsItem.getElementsByTag("img").first().attr("src");
                //String price = element.select("i[data-price]").first().attr("data-price");
                //String shopName = element.getElementsByClass("p-shop").first().text();
                String productId = goodsItem.select("a.sellPoint").first().attr("sa-data").split("'prdid':'")[1].split("'")[0];
                String description = goodsItem.getElementsByClass("title-selling-point").select("a").first().text();
                String productLink = goodsItem.select("a").first().attr("href");
                if (productLink.startsWith("//")) {
                    productLink = "https:" + productLink;
                }
                Product product = new Product();
                product.setPlatform("苏宁易购");
                product.setId("sn_" + productId);
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
            return productList;
        } catch (Exception e) {
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    static public List<Product> searchVip(String searchProductName) throws IOException {
        System.out.println("Search Vip");
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
                product.setId("vip_" + productId);
                product.setName(productName);
                product.setPrice(Double.parseDouble(price));
                product.setLink("https:" + productLink);
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
            return productList;
        }
        catch (Exception e){
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    public static Double getTaobaoPrice(String url) throws IOException {
        try {
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            //Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            String price = document.select("div[class^=highlightPrice--]").first().select("span[class^=text--]").text();
            return Double.parseDouble(price);
        }
        catch (Exception e){
            return null;
        }
    }

    public static Double getSuningPrice(String url) throws IOException {
        try {
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            String price = document.getElementById("cart2Price").text();
            Pattern pattern = Pattern.compile("\\d+\\.\\d+");
            Matcher matcher = pattern.matcher(price);
            if (matcher.find()) {
                price = matcher.group();
            }
            // System.out.println("price: " + price);
            return Double.parseDouble(price);
        }
        catch (Exception e){
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    public static Double getVipPrice(String url) throws IOException {
        try {
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            String price = document.getElementById("J-topBar-sell-price").select("em").text().replace("¥ ", "");
            System.out.println("price: " + price);
            return Double.parseDouble(price);
        }
        catch (Exception e){
            return null;
        }
    }

    public List<Product> searchJD(String productName) throws IOException {
        List<Product> productList = new ArrayList<Product>();
        String url = "https://search.jd.com/Search?keyword=" + URLEncoder.encode(productName, StandardCharsets.UTF_8);
        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put("thor", "0DE2F0D75DEF13A5510E9B2F30D5B5EF1E357066964ECDA9FDC6AC3824A6DFD979AD5F51119B20C0F15FFCB6C640C4EDF6D607DE165772998E6064CFBC8EFE2EAD7C50EDE99261DAAF6176A6628500803BD7B9CDC44A500F1D86631CCD6DD04376AEE81AA7F55600A8E990F58F35BBF6D9F6159975ED72D617B88E2061D2303C5ACF2472262ABB149C1C09AB576353A79039A922E4A37493741D5C00BF0A6B2B");
        // 解析网页, document就代表网页界面
        Document document = Jsoup.connect(url).cookies(cookies).get();
        System.out.println(document);
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
            String price = element.getElementsByClass("p-price").first().text();
            String shopName = element.getElementsByClass("p-shop").first().text();
            String productId = element.getElementsByClass("gl-item").first().attr("data-sku");
            String description = element.getElementsByTag("em").last().text();

            product.setId(productId);
            product.setName(productName);
            product.setPrice(Double.parseDouble(price.substring(1)));
            product.setSpecification(description);
            product.setImageUrl("https:" + pictUrl);
            product.setLink("https://item.jd.com/" + productId + ".html");
            product.setPlatform("京东");

            productList.add(product);
        }

        return productList;
    }

}
