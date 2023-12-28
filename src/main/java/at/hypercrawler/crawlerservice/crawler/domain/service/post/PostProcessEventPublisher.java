package at.hypercrawler.crawlerservice.crawler.domain.service.post;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.event.AddressCrawledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class PostProcessEventPublisher {
  public static final String CRAWL_ADDRESS_OUT = "crawlprocess-out-0";
  private final StreamBridge streamBridge;

  public PostProcessEventPublisher(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  public Mono<AddressCrawledMessage> send(PageNode node) {
    return Mono.fromCallable(() -> {
      AddressCrawledMessage addressCrawledMessage = createMessage(node);
      boolean result = streamBridge.send(CRAWL_ADDRESS_OUT, addressCrawledMessage);
      log.info("Sending address crawled event for address {} was {}", node.getUrl(), result ? "successful" : "unsuccessful");
      return addressCrawledMessage;
    });
  }

  public AddressCrawledMessage createMessage(PageNode node) {
    log.info("Creating address crawled message for address {}", node.getUrl());
    List<String> linksTo = node.getLinksTo().stream().map(PageNode::getUrl).toList();
    return new AddressCrawledMessage(node.getCrawlerId(), linksTo);
  }



}
