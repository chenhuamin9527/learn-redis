package com.mark.learnRedis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.mark.learnRedis.mapper")
@EnableCaching
public class LearnRedisApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnRedisApplication.class, args);
	}

}
