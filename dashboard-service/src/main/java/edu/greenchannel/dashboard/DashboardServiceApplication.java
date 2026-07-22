package edu.greenchannel.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "edu.greenchannel")
@MapperScan("edu.greenchannel.dashboard.mapper")
@EnableScheduling
public class DashboardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardServiceApplication.class, args);
    }
}
