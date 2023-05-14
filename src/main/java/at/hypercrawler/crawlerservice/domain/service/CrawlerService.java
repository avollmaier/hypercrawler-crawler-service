package at.hypercrawler.crawlerservice.domain.service;

import at.hypercrawler.crawlerservice.domain.exception.CrawlerCommunicationException;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlerService {

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

    public List<String> crawl(URL url) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_2)
                    .uri(url.toURI())
                    .build();

            CompletableFuture<HttpResponse<String>> response =
                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

            return extractUrls(result);

        } catch (URISyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new CrawlerCommunicationException(url, e);
        }
    }
}
