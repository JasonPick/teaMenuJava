package com.hellogroup.teamenu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 茶饮菜单应用启动类
 * 
 * @author HelloGroup
 */
@SpringBootApplication
@MapperScan("com.hellogroup.teamenu.infrastructure.persistence.mapper")
public class TeaMenuApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TeaMenuApplication.class, args);
    }
}
