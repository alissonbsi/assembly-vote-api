package br.com.sicred.assemblyvote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AssemblyVoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssemblyVoteApplication.class, args);
	}

}
