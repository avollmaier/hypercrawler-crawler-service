package at.hypercrawler.crawlerservice.crawler.event;

import java.util.UUID;

public record AddressCrawledMessage(UUID crawlerId, java.util.List<String> rawAddresses) {
}
