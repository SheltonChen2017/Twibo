package edu.miis.FinalProject_SpringbootHibernateDruid;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EntityScan(basePackages = {"edu.miis"})
@ComponentScan(basePackages={"edu.miis"})
@EnableJpaRepositories("edu.miis.Dao")
@EnableTransactionManagement
public class FinalProjectSpringbootHibernateDruidApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinalProjectSpringbootHibernateDruidApplication.class, args);
	}



}


