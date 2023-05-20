package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CrawlerService {

    private final StreamBridge streamBridge;

    public CrawlerService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
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
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_2)
                    .uri(address.toURI())
                    .build();

            CompletableFuture<HttpResponse<String>> response =
                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

            List<String> extractedUrls = extractUrls(result);
            publishAddressCrawledEvent(extractedUrls, crawlerId);

            return extractedUrls;

        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error while crawling address {}", address);
        }

        return new ArrayList<>();
    }

    private void publishAddressCrawledEvent(List<String> extractedUrls, UUID crawlerId) {

        for (String address : extractedUrls) {
            var addressSupplyMessage = new AddressCrawledMessage(crawlerId, address);
            var result = streamBridge.send("crawl-out-0", addressSupplyMessage);
        }
    }


}
