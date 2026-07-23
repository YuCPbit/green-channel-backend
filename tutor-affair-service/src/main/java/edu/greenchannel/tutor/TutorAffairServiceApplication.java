package edu.greenchannel.tutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 辅导员事务申请服务
 */
@SpringBootApplication(scanBasePackages = "edu.greenchannel")
public class TutorAffairServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TutorAffairServiceApplication.class, args);
    }
}
