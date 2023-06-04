package at.hypercrawler.crawlerservice.manager;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CrawlerTestDummyProvider {

    public static Supplier<List<String>> startUrls =
            () -> Arrays.asList("https://www.google.com", "https://www.bing.com");

    public static Supplier<List<SupportedContentMediaType>> contentTypesToMatch =
            () -> Arrays.asList(SupportedContentMediaType.HTML, SupportedContentMediaType.PDF);


    public static Supplier<List<String>> pathsToMatch =
            () -> List.of("http://www.foufos.gr/*");


    public static Supplier<List<String>> selectorsToMatch =
            () -> Arrays.asList(".products", "!.featured");


    public static Supplier<CrawlerAction> crawlerAction =
            () -> CrawlerAction.builder()
                    .contentTypesToMatch(contentTypesToMatch.get())
                    .pathsToMatch(pathsToMatch.get())
                    .selectorsToMatch(selectorsToMatch.get())
                    .indexName("test_index")
                    .build();


    public static Supplier<ConnectionProxy> connectionProxy =
            () -> new ConnectionProxy("localhost", 8080);
    public static Supplier<CrawlerRequestOptions> crawlerRequestOptions =
            () -> CrawlerRequestOptions.builder()
                    .requestTimeout(1000)
                    .proxy(connectionProxy.get())
                    .retries(3)
                    .headers(Collections.singletonList(new ConnectionHeader("User-Agent", "Mozilla/5.0 (compatible")))
                    .build();

    public static Supplier<CrawlerRobotOptions> robotOptions =
            () -> CrawlerRobotOptions.builder()
                    .ignoreRobotNoFollowTo(true)
                    .ignoreRobotRules(true)
                    .ignoreRobotNoIndex(true).build();

    public static Supplier<CrawlerConfig> crawlerConfig =
            () -> CrawlerConfig.builder()
                    .actions(Collections.singletonList(crawlerAction.get()))
                    .indexPrefix("crawler_")
                    .requestOptions(crawlerRequestOptions.get())
                    .robotOptions(robotOptions.get()).build();
}
