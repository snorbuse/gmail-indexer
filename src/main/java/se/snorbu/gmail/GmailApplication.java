package se.snorbu.gmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@SpringBootApplication
public class GmailApplication {

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(GmailApplication.class, args));
    }
}
