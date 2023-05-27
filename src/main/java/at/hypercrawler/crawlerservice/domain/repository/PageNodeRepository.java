package at.hypercrawler.crawlerservice.domain.repository;

import at.hypercrawler.crawlerservice.domain.model.PageNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PageNodeRepository extends Neo4jRepository<PageNode, String> {
}
