package at.hypercrawler.crawlerservice.manager;

import lombok.Builder;

import java.util.List;

@Builder
public record CrawlerAction(

        String indexName,

        List<String> pathsToMatch,

        List<String> selectorsToMatch,

        List<SupportedFileType> fileTypesToMatch

) {
}
