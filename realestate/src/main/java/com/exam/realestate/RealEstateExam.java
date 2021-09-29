package com.exam.realestate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.core.io.ResourceLoader;

@SpringBootApplication
public class RealEstateExam implements CommandLineRunner {
	@Autowired
	ResourceLoader resourceLoader;
	public static void main(String[] args) {
		SpringApplication.run(RealEstateExam.class, args);
	}

	@Override
	public void run(String... args) throws Exception
	{
		DocParser.firstStage(resourceLoader);
	}
}
