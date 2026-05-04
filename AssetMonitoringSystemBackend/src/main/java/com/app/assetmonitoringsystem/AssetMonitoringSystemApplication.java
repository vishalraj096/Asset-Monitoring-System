package com.app.assetmonitoringsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssetMonitoringSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetMonitoringSystemApplication.class, args);
    }

}
