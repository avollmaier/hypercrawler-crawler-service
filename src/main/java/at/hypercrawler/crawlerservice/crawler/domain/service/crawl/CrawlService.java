package at.hypercrawler.crawlerservice.crawler.domain.service.crawl;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.event.AddressPrioritizedMessage;
import at.hypercrawler.crawlerservice.manager.CrawlerConfig;
import at.hypercrawler.crawlerservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class CrawlService {


    private final ManagerClient managerClient;

    private final RequestOptionHttpClient requestOptionHttpClient;

    private final AddressExtractor addressExtractor;

    public CrawlService(ManagerClient managerClient, RequestOptionHttpClient requestOptionHttpClient, AddressExtractor addressExtractor) {
        this.managerClient = managerClient;
        this.requestOptionHttpClient = requestOptionHttpClient;
        this.addressExtractor = addressExtractor;
    }

    public Flux<PageNode> consumeAddressPrioritizedEvent(Flux<AddressPrioritizedMessage> flux) {
        log.info("Consuming address prioritized event");
        return flux.flatMap(e -> crawl(e.address(), e.crawlerId()));
    }


    public Mono<PageNode> crawl(URL address, UUID crawlerId) {
        log.info("Crawling address {}", address);

        return managerClient.getCrawlerConfigById(crawlerId)
                .doOnNext(config -> log.info("Crawler config for crawler {} is {}", crawlerId, config))
                .switchIfEmpty(Mono.error(new RuntimeException("No crawler config found for crawler " + crawlerId)))
                .flatMap(config -> fetch(config, address))
                .doOnNext(responseEntity -> log.info("Fetched address {} with status code {}", address, responseEntity.getStatusCode()))
                .map(e -> extractPageNode(crawlerId, address.toString(), e));
    }

    private Mono<ResponseEntity<String>> fetch(CrawlerConfig config, URL address) {
        log.info("Fetching address {}", address);
        URI uri;

        try {
            uri = address.toURI();
        } catch (URISyntaxException e) {
            return Mono.error(e);
        }


        HttpClient client = requestOptionHttpClient
                .setHeaders(config.requestOptions().headers())
                .setProxy(config.requestOptions().proxy()).build();

        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(client)).build();

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class)
                .timeout(Duration.ofMillis(config.requestOptions().requestTimeout()), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                .retryWhen(Retry.backoff(config.requestOptions().retries(), Duration.ofMillis(config.requestOptions().requestTimeout())))
                .onErrorResume(Exception.class, exception -> Mono.empty());

    }

    private PageNode extractPageNode(UUID crawlerId, String address, ResponseEntity<String> responseEntity) {
        List<String> extractedAddresses = addressExtractor.apply(responseEntity.getBody());

        PageNode node = new PageNode(
                address,
                crawlerId,
                responseEntity.getStatusCode().value(),
                Duration.ZERO,
                Objects.requireNonNull(responseEntity.getHeaders().getContentType()).getType(),
                responseEntity.getHeaders().getContentLength(),
                responseEntity.getBody());

        extractedAddresses.forEach(e -> node.addPageNode(new PageNode(e, crawlerId)));

        return node;
    }
}
