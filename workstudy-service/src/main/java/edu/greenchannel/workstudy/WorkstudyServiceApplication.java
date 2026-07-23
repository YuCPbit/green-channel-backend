package edu.greenchannel.workstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "edu.greenchannel")

public class WorkstudyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkstudyServiceApplication.class, args);
    }
}
