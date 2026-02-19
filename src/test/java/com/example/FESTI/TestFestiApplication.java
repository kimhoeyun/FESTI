package com.example.FESTI;

import org.springframework.boot.SpringApplication;

public class TestFestiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FestiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
