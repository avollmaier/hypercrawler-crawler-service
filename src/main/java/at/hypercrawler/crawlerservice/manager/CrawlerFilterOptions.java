package at.hypercrawler.crawlerservice.manager;

import java.util.List;

public record CrawlerFilterOptions(

        List<String> siteExclusionPatterns,

        List<String> queryParameterExclusionPatterns

) {
}
