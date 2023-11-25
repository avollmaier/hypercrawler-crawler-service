package at.hypercrawler.crawlerservice.crawler.domain.service.post;

import at.hypercrawler.crawlerservice.crawler.domain.model.FunctionPayload;
import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class PostProcessService {
    public static final String CRAWL_ADDRESS_OUT = "crawlprocess-out-0";
    private final StreamBridge streamBridge;
    private final PageNodeRepository pageNodeRepository;

    private final ActionHandler actionHandler;

    public PostProcessService(StreamBridge streamBridge, PageNodeRepository pageNodeRepository, ActionHandler actionHandler) {
        this.streamBridge = streamBridge;
        this.pageNodeRepository = pageNodeRepository;
        this.actionHandler = actionHandler;
    }

    public Flux<PageNode> consumeAddressPrefilterEvent(Flux<FunctionPayload<PageNode>> flux) {
        return flux.flatMap(this::postProcess);
    }

    private Mono<PageNode> postProcess(FunctionPayload<PageNode> event) {
        return Mono.just(actionHandler.handleActions(event.payload(), event.config().actions(), event.config().indexPrefix()))
                .filter(pageNode -> !pageNode.getLinksTo().isEmpty())
                .doOnNext(this::publishAddressCrawledEvent)
                .flatMap(this::savePageNode);
    }


    @Transactional
    @Retryable
    public Mono<PageNode> savePageNode(PageNode pageNode) {
        List<String> linksTo = pageNode.getLinksTo().stream().map(PageNode::getUrl).toList();
        log.info("Saving page node with parent: {} and children: {}", pageNode.getUrl(), linksTo);

        return pageNodeRepository
                .save(pageNode)
                .doOnNext(pageNode1 -> log.info("Saved page node with address {}", pageNode1.getUrl()))
                .onErrorReturn(pageNode)
                .doOnError(throwable -> log.error("Error while saving {}", throwable.getMessage()));
    }

    private void publishAddressCrawledEvent(PageNode node) {
        log.info("Sending address crawled event for address {}", node.getUrl());
        List<String> linksTo = node.getLinksTo().stream().map(PageNode::getUrl).toList();
        AddressCrawledMessage addressCrawledMessage = new AddressCrawledMessage(node.getCrawlerId(), linksTo);
        boolean result = streamBridge.send(CRAWL_ADDRESS_OUT, addressCrawledMessage);
        log.info("Sending address crawled event for address {} was {}", node.getUrl(), result ? "successful" : "unsuccessful");
    }
}
