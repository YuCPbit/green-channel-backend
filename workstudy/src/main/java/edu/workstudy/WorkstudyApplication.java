package edu.greenchannel.workstudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.greenchannel.workstudy.mapper")
public class WorkStudyApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkStudyApplication.class, args);
    }
}