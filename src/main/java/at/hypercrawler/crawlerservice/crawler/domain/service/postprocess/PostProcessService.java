package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.FunctionPayload;
import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
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
    public static final String CRAWL_ADDRESS_OUT = "crawlprocess-out-0";
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

    public Flux<PageNode> consumeAddressPrefilterEvent(Flux<FunctionPayload<PageNode>> flux) {
        return flux.flatMap(this::postProcess);
    }

    private Mono<PageNode> postProcess(FunctionPayload<PageNode> event) {
        return Mono.just(actionHandler.handleActions(event.payload(), event.config().actions(), event.config().indexPrefix()))
                .flatMap(pageNodeRepository::save)
                .doOnNext(this::publishAddressCrawledEvent);
    }


    private void publishAddressCrawledEvent(PageNode node) {
        log.info("Sending address crawled event for address {}", node.getUrl());
        List<String> linksTo = node.getLinksTo().stream().map(PageNode::getUrl).toList();
        AddressCrawledMessage addressSupplyMessage = new AddressCrawledMessage(node.getCrawlerId(), linksTo);
        boolean result = streamBridge.send(CRAWL_ADDRESS_OUT, addressSupplyMessage);
        log.info("Sending address crawled event for address {} was {}", node.getUrl(), result ? "successful" : "unsuccessful");
    }
}
