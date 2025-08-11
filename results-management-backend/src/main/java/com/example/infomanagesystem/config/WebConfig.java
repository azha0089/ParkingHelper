package com.example.infomanagesystem.config;


import com.example.infomanagesystem.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("WebConfig.addResourceHandlers");
        // 将D盘的/uploads目录映射为/files路径
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:"+ FileConstant.FILE_STORAGE_PATH);

//        /files/** ：表示所有以/files/开头的请求。
//         file:D:/uploads/ 表示将D盘的uploads文件夹作为资源根目录。
//        所有以files访问的接口都会映射到D盘uploads文件夹下 注意 yml文件中有一个 context-path /api  一定要加
//        http://localhost:8066/api/files/test.png

    }
}