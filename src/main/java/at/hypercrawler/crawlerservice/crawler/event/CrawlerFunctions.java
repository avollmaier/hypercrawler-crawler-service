package at.hypercrawler.crawlerservice.crawler.event;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.service.crawl.CrawlService;
import at.hypercrawler.crawlerservice.crawler.domain.service.postprocess.PostProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Configuration
public class CrawlerFunctions {

    private final CrawlService crawlService;

    private final PostProcessService postProcessService;

    public CrawlerFunctions(CrawlService crawlService, PostProcessService postProcessService) {
        this.crawlService = crawlService;
        this.postProcessService = postProcessService;
    }


    @Bean
    public Consumer<Flux<PageNode>> postProcess() {
        return flux -> postProcessService.consumeAddressPrefilterEvent(flux)
                .doOnNext(e -> log.info("Consuming address postProcess event {}", e))
                .subscribe();
    }

    @Bean
    public Function<Flux<AddressPrioritizedMessage>, Flux<PageNode>> crawl() {
        return crawlService::consumeAddressPrioritizedEvent;
    }


}
