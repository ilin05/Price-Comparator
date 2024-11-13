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

// Before running the application, you need to start the Edge browser with the following command:
// PS C:\Program Files (x86)\Microsoft\Edge\Application> .\msedge.exe --remote-debugging-port=9222 --user-data-dir="D:\selenium\AutomationProfile"
