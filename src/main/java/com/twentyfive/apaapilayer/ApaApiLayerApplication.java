package com.twentyfive.apaapilayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.twentyfive.apaapilayer", "com.twentyfive.authorizationflow"})
@EnableFeignClients
@EnableScheduling
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApaApiLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApaApiLayerApplication.class, args);
    }

}
