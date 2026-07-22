package edu.greenchannel.subsidy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "edu.greenchannel")
public class SubsidyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubsidyServiceApplication.class, args);
    }
}
