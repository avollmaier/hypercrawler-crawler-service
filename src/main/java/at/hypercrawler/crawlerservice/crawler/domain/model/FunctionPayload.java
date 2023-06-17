package at.hypercrawler.crawlerservice.crawler.domain.model;


import at.hypercrawler.crawlerservice.manager.CrawlerConfig;

import java.util.UUID;

public record FunctionPayload<T>(
        UUID crawlerId,
        CrawlerConfig config,
        T payload

) {
}
