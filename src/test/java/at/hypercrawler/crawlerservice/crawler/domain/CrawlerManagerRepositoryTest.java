package at.hypercrawler.crawlerservice.crawler.domain;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.repository.PageNodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@Testcontainers
class CrawlerManagerRepositoryTest {
    static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:latest")
            .withLabsPlugins(Neo4jLabsPlugin.APOC)
            .withReuse(true);


    @Autowired
    private PageNodeRepository pageNodeRepository;

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        neo4j.start();

        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }


    @Test
    void whenFindPageNodeByIdWhenNotExisting_thenNoPageNodeReturned() {
        StepVerifier.create(pageNodeRepository.findById("test")).expectNextCount(0).verifyComplete();
    }

    @Test
    void whenCreatePageNode_thenPageNodeIsInDatabase() {
        PageNode pageNode = PageNode.builder().url("fdsa").crawlerId(UUID.randomUUID()).responseCode(123).lastModifiedDateOfPage(Instant.now()).content("fdsafdsafdasfdsaf").contentLength(3241L).contentType(String.valueOf(MediaType.APPLICATION_JSON)).build();
        StepVerifier.create(pageNodeRepository.save(pageNode)).expectNextMatches(
                c -> c.getContentType().equals(MediaType.APPLICATION_JSON)).verifyComplete();
    }

}
