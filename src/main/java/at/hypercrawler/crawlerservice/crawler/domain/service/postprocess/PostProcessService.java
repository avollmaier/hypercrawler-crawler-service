package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
import at.hypercrawler.crawlerservice.manager.CrawlerAction;
import at.hypercrawler.crawlerservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class PostProcessService {
    public static final String CRAWL_ADDRESS_OUT = "crawl-out-0";
    private final StreamBridge streamBridge;
    private final ManagerClient managerClient;
    private final AbstractGraphManager graphManager;

    public PostProcessService(StreamBridge streamBridge, ManagerClient managerClient, AbstractGraphManager graphManager) {
        this.streamBridge = streamBridge;
        this.managerClient = managerClient;
        this.graphManager = graphManager;
    }

    public Flux<PageNode> consumeAddressPrefilterEvent(Flux<PageNode> flux) {
        return flux.flatMap(this::postProcess);
    }

    private Mono<PageNode> postProcess(PageNode pageNode) {
        return managerClient.getCrawlerConfigById(pageNode.getCrawlerId())
                .doOnNext(config -> log.info("Crawler config for crawler {} is {}", pageNode.getCrawlerId(), config))
                .switchIfEmpty(Mono.error(new RuntimeException("No crawler config found for crawler " + pageNode.getCrawlerId())))
                .flatMap(crawlerConfig -> Mono.just(handleActions(pageNode, crawlerConfig.actions())));

    }

    private PageNode handleActions(PageNode pageNode, List<CrawlerAction> actions) {
        log.info("Now the funny part - persisting");
        publishAddressCrawledEvent(pageNode);

        return pageNode;
    }

    private void publishAddressCrawledEvent(PageNode node) {

        for (PageNode linkNode : node.getLinksTo()) {
            AddressCrawledMessage addressSupplyMessage = new AddressCrawledMessage(node.getCrawlerId(), linkNode.getUrl());
            streamBridge.send(CRAWL_ADDRESS_OUT, addressSupplyMessage);
        }

    }


}
