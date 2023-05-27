package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.domain.model.PageNode;
import at.hypercrawler.crawlerservice.domain.repository.PageNodeRepository;
import at.hypercrawler.crawlerservice.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CrawlerService {

    private final StreamBridge streamBridge;

    private final WebClient webClient;

    private final PageNodeRepository pageNodeRepository;

    public CrawlerService(StreamBridge streamBridge, WebClient webClient, PageNodeRepository pageNodeRepository) {
        this.streamBridge = streamBridge;
        this.webClient = webClient;
        this.pageNodeRepository = pageNodeRepository;
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "(?:(?:https?|ftp):\\/\\/|www\\.)" +
                "[\\w\\d\\-_]+(?:\\.[\\w\\d\\-_]+)" +
                "[\\w\\d\\-\\.,@?^=%&amp;:/~\\+#]*" +
                "[\\w\\d\\-\\@?^=%&amp;/~\\+#]";

        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public List<String> crawl(URL address, UUID crawlerId) {
        List<String> extractedUrls = new ArrayList<>();
        try {

            AtomicReference<PageNode> pageNode = new AtomicReference<>();

            webClient
                    .get()
                    .uri(address.toURI())
                    .retrieve()
                    .toEntity(String.class)
                    .doOnSuccess(response -> {

                        pageNode.set(new PageNode(
                                address.toString(),
                                response.getStatusCode().value(),
                                Duration.ZERO,
                                response.getHeaders().getContentType(),
                                response.getHeaders().getContentLength(),
                                response.getBody()));

                        log.error(pageNode.get().toString());

                        extractedUrls.addAll(extractUrls(response.getBody()));
                    })
                    .doOnError(error -> log.error("Error while crawling address {}", address))
                    .block();

            if (pageNode.get() != null) pageNodeRepository.save(pageNode.get());
            publishAddressCrawledEvent(extractedUrls, crawlerId);

            return extractedUrls;

        } catch (URISyntaxException e) {
            log.error("Error while crawling address {}", address);
        }

        return extractedUrls;
    }

    private void publishAddressCrawledEvent(List<String> extractedUrls, UUID crawlerId) {
        for (String address : extractedUrls) {
            var addressSupplyMessage = new AddressCrawledMessage(crawlerId, address);
            var result = streamBridge.send("crawl-out-0", addressSupplyMessage);
        }
    }


}
