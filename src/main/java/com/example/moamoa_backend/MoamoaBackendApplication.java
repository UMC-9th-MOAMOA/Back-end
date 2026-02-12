package com.example.moamoa_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoamoaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoamoaBackendApplication.class, args);
	}

}
