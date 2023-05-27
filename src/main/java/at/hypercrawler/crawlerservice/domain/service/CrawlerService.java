package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.domain.model.PageNode;
import at.hypercrawler.crawlerservice.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CrawlerService {

    private final StreamBridge streamBridge;

    private final WebClient webClient;

    private final AbstractGraphManager graphManager;

    public CrawlerService(StreamBridge streamBridge, WebClient webClient, AbstractGraphManager graphManager) {
        this.streamBridge = streamBridge;
        this.webClient = webClient;
        this.graphManager = graphManager;
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

    public void crawl(URL address, UUID crawlerId) {
        List<String> extractedUrls = new ArrayList<>();
        try {
            webClient
                    .get()
                    .uri(address.toURI())
                    .retrieve()
                    .toEntity(String.class)
                    .subscribe(response -> {
                        log.info("Crawled address {} with status code {}", address, response.getStatusCode());

                        PageNode pageNode = new PageNode(
                                address.toString(),
                                response.getStatusCode().value(),
                                Duration.ZERO,
                                Objects.requireNonNull(response.getHeaders().getContentType()).getType(),
                                response.getHeaders().getContentLength(),
                                response.getBody());


                        extractedUrls.addAll(extractUrls(response.getBody()));
                        graphManager.persistParentPageNode(pageNode);

                        log.error("Extracted urls: {}", extractedUrls);
                        publishAddressCrawledEvent(extractedUrls, crawlerId);
                    });
        } catch (URISyntaxException e) {
            log.error("Error while crawling address {}", address);
        }
    }



    private void publishAddressCrawledEvent(List<String> extractedUrls, UUID crawlerId) {
        for (String address : extractedUrls) {
            var addressSupplyMessage = new AddressCrawledMessage(crawlerId, address);
            var result = streamBridge.send("crawl-out-0", addressSupplyMessage);
        }
    }


}
