package com.myks.myksbk.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.work-thumbnail-dir}")
    private String thumbnailDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + thumbnailDir + "/")
                .setCachePeriod(3600);
    }
}