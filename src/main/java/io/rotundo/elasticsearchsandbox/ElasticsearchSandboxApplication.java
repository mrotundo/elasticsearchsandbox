package io.rotundo.elasticsearchsandbox;

import io.rotundo.elasticsearchsandbox.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ElasticsearchSandboxApplication {

	@Autowired
	public ElasticsearchService elasticsearchService;



	public static void main(String[] args) {
		SpringApplication.run(ElasticsearchSandboxApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("It has begun!");

			//commented out - will run the bulk insert indefinitely
			/*
			while(true){
				elasticsearchService.createBulkRecords();
			}
 			*/
		};
	}




}
