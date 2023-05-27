package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.domain.model.PageNode;
import at.hypercrawler.crawlerservice.domain.repository.PageNodeRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.UnaryOperator;

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
