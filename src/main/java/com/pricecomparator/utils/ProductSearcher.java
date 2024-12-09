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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductSearcher {
    // static EdgeOptions options = new EdgeOptions();

    static public List<Product> searchTaobao(String searchProductName) throws InterruptedException {
        System.out.println("Search Taobao");
        try {
            EdgeOptions options = new EdgeOptions();
//            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9226");
//            options.addArguments("--headless=old");
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
            // System.out.println(document);
            Elements goodsItems = document.select("a[class^=Card--doubleCardWrapper--]");
            int count = 0;
            for(Element goodsItem : goodsItems){
                // System.out.println(goodsItem);
                if(goodsItem.select("div[class^=Title--title--]").select("span").text().length() == 0){
                    continue;
                }
                // System.out.println("hello1");
                String productLink = goodsItem.attr("href");
                System.out.println(productLink);
                // String pattern = "i(\\d+)\\.htm";
                String pattern = "id=(\\d+)"; // 匹配商品id
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
                // System.out.println("hello2");
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
                count++;
                if(count == 10){
                    break;
                }
                //String pictUrl = goodsItem.select("img").first().attr("src");
                //System.out.println(goodsItem);
//                System.out.println("商品id: " + productId);
//                System.out.println("商品名称: " + description);
//                System.out.println("价格: " + price);
//                System.out.println("图片路径: " + pictUrl);
//                System.out.println("商品链接: " + productLink);
//                System.out.println("--------------------------");
            }
            System.out.println(count);
            //return null;
            return productList;
        } catch (Exception e){
            return null;
        }
    }

    static public List<Product> searchSuning(String searchProductName) throws InterruptedException {
        System.out.println("Search Suning");
        try {
            EdgeOptions options = new EdgeOptions();
//            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9223");
//            options.addArguments("--headless=old");
            List<Product> productList = new ArrayList<Product>();
            String url = "https://search.suning.com/" + searchProductName + "/";
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9223");
            options.addArguments("--headless=old");
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
            EdgeOptions options = new EdgeOptions();
//            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9224");
//            options.addArguments("--headless=old");
            List<Product> productList = new ArrayList<Product>();
            String url = "https://category.vip.com/suggest.php?keyword=" + searchProductName;
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9224");
            options.addArguments("--headless=old");
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            //System.out.println(document);
            Elements goodsItems = document.select(".c-goods-item");
            //System.out.println(goodsItems);
            // 取前10个内容
            int count = 0;
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
                count++;
                if(count == 10){
                    break;
                }
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

    static public List<Product> searchXiaoMiYouPin(String searchProductName) throws IOException {
        System.out.println("Search XiaoMiYouPin");
        try {
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9225");
            options.addArguments("--headless=old");
            List<Product> productList = new ArrayList<Product>();
            String url = "https://www.xiaomiyoupin.com/search?keyword=" + searchProductName;
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            // System.out.println(document);
            Elements goodsItems = document.select(".pro-item");
            System.out.println("hello from xiaomi");
            // System.out.println(goodsItems);

            for(Element goodsItem : goodsItems){
                // System.out.println(goodsItem);
                // 提取商品名称
                String productName = goodsItem.select(".pro-name").attr("title");
                // 提取价格
                String price = goodsItem.select(".pro-price .m-num").text();
                // 提取商品链接
                String productLink = goodsItem.attr("data-src");
                // 提取商品id
                int startIndex = productLink.indexOf("gid=");
                String productId = productLink.substring(startIndex + 4);
                String imageUrl = goodsItem.select(".pro-img").select("img").attr("src");
                Product product = new Product();
                product.setPlatform("小米有品");
                product.setId("xm_" + productId);
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
            return productList;
        }
        catch (Exception e){
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    public static Double getTaobaoPrice(String url) throws IOException {
        try {
            EdgeOptions options = new EdgeOptions();
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
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9223");
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
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9224");
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

    public static Double getXMPrice(String url) throws IOException {
        try {
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9225");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            Elements priceElements = document.select(".price .value");
            if (!priceElements.isEmpty()) {
                String price = priceElements.first().text();
                System.out.println("商品价格: " + price);
                return Double.valueOf(price);
            } else {
                System.out.println("未找到价格信息");
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Double getJDPrice(String url) throws IOException {
        try {
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
            options.addArguments("--headless=old"); // 无头模式
            WebDriver driver = new EdgeDriver(options);
            driver.get(url);
            Thread.sleep(5000); //等页面加载完成
            String pageSource = driver.getPageSource();
            driver.quit();
            Document document = Jsoup.parse(pageSource);
            System.out.println(document);
            Elements priceElements = document.select(".finalPrice .price");
            if (!priceElements.isEmpty()) {
                String price = priceElements.first().text();
                System.out.println("商品价格: " + price);
                return Double.valueOf(price);
            } else {
                System.out.println("未找到价格信息");
                return null;
            }
        } catch (Exception e) {
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

    public static List<Product> searchTogether(String productName) {
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

        return productList;
    }

    public static List<Product> checkFavoriteProductsPrice(List<Product> toCheckProducts){
        try {
            for(Product product : toCheckProducts){
                System.out.println(product);
            }
            System.out.println("--------------------");
            List<Product> TaoBaoProducts = new ArrayList<Product>();
            List<Product> SuningProducts = new ArrayList<Product>();
            List<Product> VipProducts = new ArrayList<Product>();
            List<Product> XMProducts = new ArrayList<Product>();
            for(Product product : toCheckProducts){
                String platform = product.getPlatform();
                if(platform.equals("淘宝")){
                    TaoBaoProducts.add(product);
                }else if(platform.equals("苏宁易购")){
                    SuningProducts.add(product);
                }else if(platform.equals("唯品会")){
                    VipProducts.add(product);
                }else if(platform.equals("小米有品")){
                    XMProducts.add(product);
                }
            }
            CompletableFuture<List<Product>> taobaoFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    for(Product taobaoProduct : TaoBaoProducts){
                        String url = taobaoProduct.getLink();
                        Double price = ProductSearcher.getTaobaoPrice(url);
                        Double previousPrice = taobaoProduct.getPrice();
                        taobaoProduct.setPreviousPrice(previousPrice);
                        taobaoProduct.setPrice(price);
                    }
                    return TaoBaoProducts;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<List<Product>> suningFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    for(Product suningProduct : SuningProducts){
                        String url = suningProduct.getLink();
                        Double price = ProductSearcher.getSuningPrice(url);
                        Double previousPrice = suningProduct.getPrice();
                        suningProduct.setPreviousPrice(previousPrice);
                        suningProduct.setPrice(price);
                    }
                    return SuningProducts;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<List<Product>> vipFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    for(Product vipProduct : VipProducts){
                        String url = vipProduct.getLink();
                        Double price = ProductSearcher.getVipPrice(url);
                        Double previousPrice = vipProduct.getPrice();
                        vipProduct.setPreviousPrice(previousPrice);
                        vipProduct.setPrice(price);
                    }
                    return VipProducts;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<List<Product>> xmFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    for(Product xmProduct : XMProducts){
                        String url = xmProduct.getLink();
                        Double price = ProductSearcher.getXMPrice(url);
                        Double previousPrice = xmProduct.getPrice();
                        xmProduct.setPreviousPrice(previousPrice);
                        xmProduct.setPrice(price);
                    }
                    return XMProducts;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(taobaoFuture, suningFuture, vipFuture, xmFuture);
            List<Product> resultProducts = new ArrayList<Product>();
            allFutures.thenApply(v -> {
                try {
                    resultProducts.addAll(taobaoFuture.get());
                    resultProducts.addAll(suningFuture.get());
                    resultProducts.addAll(vipFuture.get());
                    resultProducts.addAll(xmFuture.get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }).join();
            for (Product product : resultProducts) {
                System.out.println(product);
            }
            System.out.println("共搜索到" + resultProducts.size() + "个商品");
            return resultProducts;
        }
        catch (Exception e){
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException(e);
        }
    }

    public static List<Product> searchProductInOneThread(String productName){
        try{
            List<Product> productList = new ArrayList<Product>();
            List<Product> taobaoList = ProductSearcher.searchTaobao(productName);
            List<Product> suningList = ProductSearcher.searchSuning(productName);
            List<Product> vipList = ProductSearcher.searchVip(productName);
            List<Product> xmList = ProductSearcher.searchXiaoMiYouPin(productName);
            if(taobaoList != null){
                productList.addAll(taobaoList);
            }
            if(suningList != null){
                productList.addAll(suningList);
            }
            if(vipList != null){
                productList.addAll(vipList);
            }
            if(xmList != null){
                productList.addAll(xmList);
            }
            return productList;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
