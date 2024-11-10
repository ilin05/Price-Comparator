package com.pricecomparator.service;

import com.pricecomparator.entities.Product;
import com.pricecomparator.mapper.UserMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

@Component
public class ProductScrapter {

    private final UserMapper userMapper;

    public ProductScrapter(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<Product> getProduct(String productName) throws IOException {
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
