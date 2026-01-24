package com.rokyai.dnd14th1backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader(ApiConstants.API_VERSION_HEADER)
                .setDefaultVersion(ApiConstants.DEFAULT_API_VERSION);
    }
}
