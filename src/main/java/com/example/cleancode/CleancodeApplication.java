package com.example.cleancode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@ComponentScan(basePackages = {
		"com.example.cleancode"})
@EnableMongoRepositories("com.example.cleancode.image.repository")
@SpringBootApplication
public class CleancodeApplication {

	public static void main(String[] args) {

		SpringApplication.run(CleancodeApplication.class, args);
	}

}
