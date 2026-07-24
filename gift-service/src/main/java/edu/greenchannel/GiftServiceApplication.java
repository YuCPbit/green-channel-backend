package edu.greenchannel;

//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.greenchannel.gift.mapper")
public class GiftServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiftServiceApplication.class, args);
    }



}