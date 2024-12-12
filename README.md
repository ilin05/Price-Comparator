在开启后端之前，先在 `C:\Program Files (x86)\Microsoft\Edge\Application` 目录下运行这个指令：

```powershell
.\msedge.exe --remote-debugging-port=9222 --user-data-dir="D:\selenium\AutomationProfile"
```

```powershell
.\msedge.exe --remote-debugging-port=9223 --user-data-dir="D:\selenium\AutomationProfile9223"
```

```powershell
.\msedge.exe --remote-debugging-port=9224 --user-data-dir="D:\selenium\AutomationProfile9224"
```

```powershell
.\msedge.exe --remote-debugging-port=9225 --user-data-dir="D:\selenium\AutomationProfile9225"
```



打开浏览器之后，在9222端口的浏览器上登录淘宝，在9223端口的浏览器上登录苏宁易购，在9222端口的浏览器上登录唯品会，在9225端口的浏览器上登录小米有品，共4个网站



QQ邮箱授权码：`xqknugfuqqykcdeb`





可以设置`EdgeBrowserInitializer.java`文件，在项目启动时执行。

```java
package com.pricecomparator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EdgeBrowserInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 定义 Edge 浏览器的路径
        String edgePath = "\"C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe\"";
        // String edgePath = "msedge.exe";
        // 定义四个命令
        String[] commands = {
                edgePath + " --remote-debugging-port=9222 --user-data-dir=\"D:\\selenium\\AutomationProfile\"",
                edgePath + " --remote-debugging-port=9223 --user-data-dir=\"D:\\selenium\\AutomationProfile9223\"",
                edgePath + " --remote-debugging-port=9224 --user-data-dir=\"D:\\selenium\\AutomationProfile9224\"",
                edgePath + " --remote-debugging-port=9225 --user-data-dir=\"D:\\selenium\\AutomationProfile9225\""
        };

        // 执行每个命令
        for (String command : commands) {
            //executeCommand(command);
            Process process = Runtime.getRuntime().exec(command);
        }
    }
}
```





**多级品类：**

服装与配饰-男装

服装与配饰-女装

服装与配饰-配饰

电子产品-手机与配件

电子产品-电脑与配件

电子产品-家用电器

电子产品-摄影与摄像

家居与生活-家具

家居与生活-家纺

家居与生活-厨房用品

家居与生活-清洁用品

美妆与个人护理-护肤品

美妆与个人护理-彩妆

美妆与个人护理-个人护理

食品与饮料-零食

食品与饮料-饮料

食品与饮料-生鲜食品

运动与户外-运动服装

运动与户外-健身器材

运动与户外-户外装备

母婴用品-婴儿服装

母婴用品-婴儿用品

母婴用品-孕妇用品

图书与文具-图书

图书与文具-文具

汽车与配件-汽车

汽车与配件-汽车配件

宠物用品-宠物食品

宠物用品-宠物用品

礼品与定制-礼品

礼品与定制-定制产品



服装与配饰-男装，服装与配饰-女装，服装与配饰-配饰，电子产品-手机与配件，电子产品-电脑与配件，电子产品-家用电器，电子产品-摄影与摄像，家居与生活-家具，家居与生活-家纺，家居与生活-厨房用品，家居与生活-清洁用品，美妆与个人护理-护肤品，美妆与个人护理-彩妆，美妆与个人护理-个人护理，食品与饮料-零食，食品与饮料-饮料，食品与饮料-生鲜食品，运动与户外-运动服装，运动与户外-健身器材，运动与户外-户外装备，母婴用品-婴儿服装，母婴用品-婴儿用品，母婴用品-孕妇用品，图书与文具-图书，图书与文具-文具，汽车与配件-汽车，汽车与配件-汽车配件，宠物用品-宠物食品，宠物用品-宠物用品，礼品与定制-礼品，礼品与定制-定制产品
