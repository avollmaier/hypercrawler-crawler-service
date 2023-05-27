package at.hypercrawler.crawlerservice.domain.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CrawlerConfig {

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }
}
