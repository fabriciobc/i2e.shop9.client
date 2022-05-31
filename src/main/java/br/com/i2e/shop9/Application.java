package br.com.i2e.shop9;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
	@Autowired
	Environment env;
	
//	@Bean
//	public CommandLineRunner run2() throws Exception {
//		return args -> {
//			System.out.println( "\n\n\nurl: " + env.getProperty( "i2e.shop9.url" ) );
//		};
//	}
}
