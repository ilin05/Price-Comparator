package com.pricecomparator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.pricecomparator.mapper")
public class PriceComparatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceComparatorApplication.class, args);
    }

}
