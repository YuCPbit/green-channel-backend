package edu.greenchannel.gift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "edu.greenchannel")
public class GiftServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiftServiceApplication.class, args);
    }
}
