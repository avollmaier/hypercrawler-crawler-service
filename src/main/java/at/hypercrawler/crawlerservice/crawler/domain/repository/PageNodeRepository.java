package at.hypercrawler.crawlerservice.crawler.domain.repository;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PageNodeRepository extends Neo4jRepository<PageNode, String> {
}
