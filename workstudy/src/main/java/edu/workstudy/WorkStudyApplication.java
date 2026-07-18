package edu.workstudy; // 包名必须和文件夹路径一致

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.workstudy.mapper") // 扫描Mapper接口
public class WorkStudyApplication { // 类名必须和文件名完全一致
    public static void main(String[] args) {
        SpringApplication.run(WorkStudyApplication.class, args);
    }
}