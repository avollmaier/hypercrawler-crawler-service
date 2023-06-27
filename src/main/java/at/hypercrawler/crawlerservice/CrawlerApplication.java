package at.hypercrawler.crawlerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableNeo4jAuditing
public class CrawlerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CrawlerApplication.class, args);

  }


}
