package io.rotundo.elastictest;

import io.rotundo.elastictest.service.ElasticsearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;


@SpringBootApplication
public class ElastictestApplication {

	@Autowired
	public ElasticsearchService elasticsearchService;



	public static void main(String[] args) {
		SpringApplication.run(ElastictestApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("It has begun!");


/*
			while(true){
				elasticsearchService.createBulkRecords();
			}

 */
		};
	}




}
