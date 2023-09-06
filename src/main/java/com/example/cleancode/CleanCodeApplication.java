package com.example.cleancode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@ComponentScan(basePackages = {
		"com.example.cleancode"})
@EnableMongoRepositories("com.example.cleancode.song.repository")
@SpringBootApplication
public class CleanCodeApplication {

	public static void main(String[] args) {

		SpringApplication.run(CleanCodeApplication.class, args);
	}

}
