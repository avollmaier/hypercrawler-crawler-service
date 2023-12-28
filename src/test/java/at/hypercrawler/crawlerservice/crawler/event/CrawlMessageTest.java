package at.hypercrawler.crawlerservice.crawler.event;

import at.hypercrawler.crawlerservice.crawler.domain.model.FunctionPayload;
import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.manager.CrawlerTestDummyProvider;
import at.hypercrawler.crawlerservice.manager.ManagerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;


@FunctionalSpringBootTest
@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
class CrawlMessageTest {

    @MockBean
    ManagerClient managerClient;

    private MockWebServer mockWebServer;

    @Autowired
    private FunctionCatalog catalog;


    @Autowired
    private OutputDestination output;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();

        WebClient.builder().baseUrl(mockWebServer.url("/").url().toString()).build();

    }

    @Test
    void whenPrioritizeWithRunningCrawler_thenMessageSend() throws IOException {

        URL address = mockWebServer.url("/").url();
        UUID uuid = UUID.randomUUID();
        int responseCode = 200;
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        MediaType mediaType = MediaType.TEXT_PLAIN;

        when(managerClient.getCrawlerConfigById(uuid)).then(invocation -> Mono.just(CrawlerTestDummyProvider.crawlerConfig.get()));

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(responseCode)
                        .setHeader(HttpHeaders.CONTENT_TYPE, mediaType)
                        .setBody("<a href=\"https://www.orf.at\">ORF</a><a href=\"../bilder\">ORF</a>")
                        .setHeader(HttpHeaders.LAST_MODIFIED, zonedDateTime)
        );

        Function<Flux<AddressPrioritizedMessage>, Flux<FunctionPayload<PageNode>>> crawl = catalog.lookup(Function.class, "crawl");

        Flux<AddressPrioritizedMessage> addressSupplyMessageFlux = Flux.just(new AddressPrioritizedMessage(uuid, address));
        StepVerifier.create(crawl.apply(addressSupplyMessageFlux))
                .assertNext(pageNodeFunctionPayload -> {
                    assertEquals(address.toString(), pageNodeFunctionPayload.payload().getUrl());
                    assertEquals(Collections.emptyList(), pageNodeFunctionPayload.payload().getIndices());
                    assertEquals(uuid, pageNodeFunctionPayload.payload().getCrawlerId());
                    assertEquals(responseCode, pageNodeFunctionPayload.payload().getResponseCode());
                    assertEquals(mediaType, pageNodeFunctionPayload.payload().getContentType());
                    assertEquals(63L, pageNodeFunctionPayload.payload().getContentLength());
                    assertEquals("<a href=\"https://www.orf.at\">ORF</a><a href=\"../bilder\">ORF</a>", pageNodeFunctionPayload.payload().getContent());
                    assertEquals(2, pageNodeFunctionPayload.payload().getLinksTo().size());
                })
                .verifyComplete();
    }


    @Test
    void whenCrawlWithNOCrawlerConfig_thenNoMessageSend() throws IOException {

        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();

        when(managerClient.getCrawlerConfigById(uuid)).then(invocation -> Mono.empty());

        Function<Flux<AddressPrioritizedMessage>, Flux<FunctionPayload<PageNode>>> crawl = catalog.lookup(Function.class, "crawl");


        Flux<AddressPrioritizedMessage> addressSupplyMessageFlux = Flux.just(new AddressPrioritizedMessage(uuid, address));
        StepVerifier.create(crawl.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();

        assertNull(output.receive());

    }
}
