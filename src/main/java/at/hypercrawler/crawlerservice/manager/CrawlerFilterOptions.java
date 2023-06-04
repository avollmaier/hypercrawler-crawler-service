package at.hypercrawler.crawlerservice.manager;

import lombok.Builder;

import java.util.List;

@Builder
public record CrawlerFilterOptions(

        List<String> siteExclusionPatterns,

        List<String> queryParameterExclusionPatterns

) {
}
