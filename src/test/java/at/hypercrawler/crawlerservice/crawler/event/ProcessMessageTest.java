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
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

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
    void whenProcess_thenMessageSend() throws IOException {
        URL address = new URL("http://www.google.com");
        UUID uuid = UUID.randomUUID();


        Function<Flux<FunctionPayload<PageNode>>, Flux<AddressCrawledMessage>> process =

                catalog.lookup(Function.class, "process");


        Flux<FunctionPayload<PageNode>> addressSupplyMessageFlux = Flux.just(new FunctionPayload<>(uuid, CrawlerTestDummyProvider.crawlerConfig.get(), new PageNode(address.toString(), uuid, 200, Instant.now(), MediaType.TEXT_HTML, 12L, address.toString()))
        );


        StepVerifier.create(process.apply(addressSupplyMessageFlux))
                .verifyComplete();


        assertThat(objectMapper.readValue(output.receive().getPayload(), AddressPrioritizedMessage.class))
                .isEqualTo(new AddressPrioritizedMessage(uuid, address));

    }
}
