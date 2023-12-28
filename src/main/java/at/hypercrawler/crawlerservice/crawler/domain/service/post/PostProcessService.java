package at.hypercrawler.crawlerservice.crawler.domain.service.post;

import at.hypercrawler.crawlerservice.crawler.domain.model.FunctionPayload;
import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class PostProcessService {
  private final PageNodeRepository pageNodeRepository;
  private final ActionHandler actionHandler;
  private final PostProcessEventPublisher addressCrawledEventPublisher;

  public PostProcessService(PageNodeRepository pageNodeRepository, ActionHandler actionHandler,
                            PostProcessEventPublisher addressCrawledEventPublisher) {
    this.pageNodeRepository = pageNodeRepository;
    this.actionHandler = actionHandler;
    this.addressCrawledEventPublisher = addressCrawledEventPublisher;
  }

  @Transactional
  public Flux<AddressCrawledMessage> consumeAddressCrawledEvent(Flux<FunctionPayload<PageNode>> flux) {
    return flux.flatMap(this::postProcess).doOnNext(e -> log.error("HII Alois"));
  }

  private Mono<AddressCrawledMessage> postProcess(FunctionPayload<PageNode> event) {
    return Mono.just(actionHandler.handleActions(event.payload(), event.config().actions(), event.config().indexPrefix()))
               .filter(pageNode -> !pageNode.getLinksTo().isEmpty())
               .flatMap(this::savePageNode)
               .flatMap(addressCrawledEventPublisher::send);

  }

    private Mono<PageNode> savePageNode(PageNode pageNode) {
        List<String> linksTo = pageNode.getLinksTo().stream().map(PageNode::getUrl).toList();
        log.info("Saving page node with parent: {} and children: {}", pageNode.getUrl(), linksTo);

    return pageNodeRepository
               .save(pageNode)
               .doOnNext(pageNode1 -> log.info("Saved page node with address {}", pageNode1.getUrl()))
               .doOnError(throwable -> log.error("Error while saving page node with address {}", pageNode.getUrl(), throwable));
  }
}