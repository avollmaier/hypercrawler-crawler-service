package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import org.springframework.stereotype.Component;

@Component
public class GraphManager implements AbstractGraphManager {

    private final PageNodeRepository pageNodeRepository;


    public GraphManager(PageNodeRepository pageNodeRepository) {
        this.pageNodeRepository = pageNodeRepository;
    }

    public void persistParentPageNode(PageNode pageNode) {
        if (pageNodeRepository.existsById(pageNode.getUrl())) {
            pageNodeRepository.delete(pageNode);
            pageNodeRepository.save(pageNode);
        } else {
            pageNodeRepository.save(pageNode);
        }
    }
}
