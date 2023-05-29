package at.hypercrawler.crawlerservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "hypercrawler.crawler-service.client")
public record ClientProperties(

        @NotNull
        URI managerServiceUri

) {
}
