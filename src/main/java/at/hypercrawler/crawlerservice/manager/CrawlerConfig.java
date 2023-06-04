package at.hypercrawler.crawlerservice.manager;

import lombok.Builder;

import java.util.List;

@Builder
public record CrawlerConfig(

        String indexPrefix,

        CrawlerFilterOptions filterOptions,

        CrawlerRequestOptions requestOptions,

        CrawlerRobotOptions robotOptions,

        List<CrawlerAction> actions

) {
}
