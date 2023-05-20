package at.hypercrawler.crawlerservice.event;

import at.hypercrawler.crawlerservice.domain.service.CrawlerService;
import com.sun.jdi.VoidType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Configuration
public class CrawlerFunctions {

    private final CrawlerService crawlerService;

    public CrawlerFunctions(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Bean
    public Function<Flux<AddressPrioritizedMessage>, Mono<Void>> crawl() {
        return addressSupplyMessageFlux -> addressSupplyMessageFlux.map(addressPrioritizedMessage -> {
            log.info("Crawling address {}", addressPrioritizedMessage.address());

            List<String> extractedUrls = crawlerService.crawl(addressPrioritizedMessage.address(), addressPrioritizedMessage.crawlerId());
            log.info("Extracted urls: {}", extractedUrls);

            log.info("COUNT: {}", extractedUrls.size());
            return Mono.empty();
        }).then();
    }



}
