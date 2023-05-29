package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
import at.hypercrawler.crawlerservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PostProcessService {
    public static final String CRAWL_ADDRESS_OUT = "crawl-out-0";
    private final StreamBridge streamBridge;
    private final ManagerClient managerClient;
    private final PageNodeRepository pageNodeRepository;

    private final ActionHandler actionHandler;

    public PostProcessService(StreamBridge streamBridge, ManagerClient managerClient, PageNodeRepository pageNodeRepository, ActionHandler actionHandler) {
        this.streamBridge = streamBridge;
        this.managerClient = managerClient;
        this.pageNodeRepository = pageNodeRepository;
        this.actionHandler = actionHandler;
    }

    public Flux<PageNode> consumeAddressPrefilterEvent(Flux<PageNode> flux) {
        return flux.flatMap(this::postProcess);
    }

    private Mono<PageNode> postProcess(PageNode pageNode) {
        return managerClient.getCrawlerConfigById(pageNode.getCrawlerId())
                .doOnNext(config -> log.info("Crawler config for crawler {} is {}", pageNode.getCrawlerId(), config))
                .switchIfEmpty(Mono.error(new RuntimeException("No crawler config found for crawler " + pageNode.getCrawlerId())))
                .flatMap(crawlerConfig -> Mono.just(actionHandler.handleActions(pageNode, crawlerConfig.actions(), crawlerConfig.indexPrefix())))
                .map(pageNodeRepository::save)
                .doOnNext(this::publishAddressCrawledEvent);

    }


    private void publishAddressCrawledEvent(PageNode node) {
        for (PageNode linkNode : node.getLinksTo()) {
            AddressCrawledMessage addressSupplyMessage = new AddressCrawledMessage(node.getCrawlerId(), linkNode.getUrl());
            streamBridge.send(CRAWL_ADDRESS_OUT, addressSupplyMessage);
        }
    }
}
