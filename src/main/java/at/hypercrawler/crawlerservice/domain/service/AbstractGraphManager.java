package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.domain.model.PageNode;

public interface AbstractGraphManager {
    void persistParentPageNode(PageNode pageNode);
}
