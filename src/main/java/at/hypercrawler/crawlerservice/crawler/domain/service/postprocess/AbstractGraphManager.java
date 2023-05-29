package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;

public interface AbstractGraphManager {
    void persistParentPageNode(PageNode pageNode);
}
