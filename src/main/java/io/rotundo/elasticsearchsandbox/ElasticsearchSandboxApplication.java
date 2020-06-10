package io.rotundo.elasticsearchsandbox;

import io.rotundo.elasticsearchsandbox.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;


@SpringBootApplication
public class ElasticsearchSandboxApplication {

	@Autowired
	public ElasticsearchService elasticsearchService;


	public static void main(String[] args) {

		Integer port = 8080;
		boolean portInUse = true;

		while(portInUse){
			if(isLocalPortInUse(port)){
				port++;
			}
			else{
				portInUse=false;
			}
		}
		System.out.println("RUNNING ON PORT: "+ port);

		SpringApplication app = new SpringApplication(ElasticsearchSandboxApplication.class);
		app.setDefaultProperties(Collections
				.singletonMap("server.port", port.toString()));
		app.run(args);

	}

	protected static boolean isLocalPortInUse(int port) {
		try {
			// ServerSocket try to open a LOCAL port
			new ServerSocket(port).close();
			// local port can be opened, it's available
			return false;
		} catch(IOException e) {
			// local port cannot be opened, it's in use
			return true;
		}
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
