package at.hypercrawler.crawlerservice.crawler.domain.repository;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageNodeRepository extends ReactiveNeo4jRepository<PageNode, String> {

}
