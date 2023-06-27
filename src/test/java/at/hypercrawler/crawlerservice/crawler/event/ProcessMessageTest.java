package at.hypercrawler.crawlerservice.crawler.event;

import at.hypercrawler.crawlerservice.crawler.domain.model.FunctionPayload;
import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.manager.CrawlerTestDummyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(TestChannelBinderConfiguration.class)
@Testcontainers
class ProcessMessageTest {

    @Autowired
    private FunctionCatalog catalog;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private OutputDestination output;


    static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:latest")
            .withLabsPlugins(Neo4jLabsPlugin.APOC)
            .withReuse(true);


    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        neo4j.start();

        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    void whenProcessWithoutCrawledLinks_thenNoMessageSend() throws IOException {
        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();


        Function<Flux<FunctionPayload<PageNode>>, Flux<AddressCrawledMessage>> process =

                catalog.lookup(Function.class, "process");

        PageNode pageNode = PageNode.builder().url("https://example.com").crawlerId(UUID.randomUUID()).lastModifiedDateOfPage(Instant.now()).responseCode(342).contentLength(1243L).contentType(String.valueOf(MediaType.TEXT_HTML)).content("test").build();

        Flux<FunctionPayload<PageNode>> addressCrawledResponse = Flux.just(new FunctionPayload<>(uuid, CrawlerTestDummyProvider.crawlerConfig.get(), pageNode)
        );


        StepVerifier.create(process.apply(addressCrawledResponse))
                .expectNextCount(0)
                .verifyComplete();

        assertNull(output.receive());

    }

    @Test
    void whenProcessWithLinks_thenMessageSend() throws IOException {
        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();


        Function<Flux<FunctionPayload<PageNode>>, Flux<AddressCrawledMessage>> process =

                catalog.lookup(Function.class, "process");


        PageNode pageNode = PageNode.builder().url("https://example.com").crawlerId(uuid).lastModifiedDateOfPage(Instant.now()).responseCode(342).contentLength(1243L).contentType(String.valueOf(MediaType.TEXT_HTML)).content("test").build();
        pageNode.addPageNode(PageNode.builder().url(address.toString()).crawlerId(uuid).build());

        Flux<FunctionPayload<PageNode>> addressSupplyMessageFlux = Flux.just(new FunctionPayload<>(uuid, CrawlerTestDummyProvider.crawlerConfig.get(), pageNode)
        );


        StepVerifier.create(process.apply(addressSupplyMessageFlux))
                .expectNextCount(0)
                .verifyComplete();


        assertThat(objectMapper.readValue(output.receive().getPayload(), AddressCrawledMessage.class))
                .isEqualTo(new AddressCrawledMessage(uuid, List.of(address.toString())));

    }
}
