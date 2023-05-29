package at.hypercrawler.crawlerservice.manager;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record CrawlerRequestOptions(

        @Valid
        ConnectionProxy proxy,

        String proxyHost,

        Integer requestTimeout,

        Integer retries,

        List<ConnectionHeader> headers
) {

}
