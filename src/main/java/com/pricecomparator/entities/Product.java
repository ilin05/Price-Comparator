package com.pricecomparator.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Product {
    private String id;
    private String name;
    private double price;
    private String platform;
    private String category;
    private String specification;
    private String barcodeUrl;
    private String imageUrl;
    private String link;
    private double previousPrice;
}
