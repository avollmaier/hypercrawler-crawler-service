package at.hypercrawler.crawlerservice.crawler.domain.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Node
@Data
public class PageNode {
    @Id
    String url;

    UUID crawlerId;

    Integer responseCode;

    Duration responseTime;

    String contentType;

    Long contentLength;

    String content;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    Set<PageNode> linksTo;

    public PageNode(String url, UUID crawlerId) {
        this.url = url;
        this.crawlerId = crawlerId;
    }

    public PageNode(String address, UUID crawlerId, int value, Duration zero, String type, long contentLength, String body) {
        this.url = address;
        this.crawlerId = crawlerId;
        this.responseCode = value;
        this.responseTime = zero;
        this.contentType = type;
        this.contentLength = contentLength;
        this.content = body;
    }

    public void addPageNode(PageNode pageNode) {
        this.linksTo.add(pageNode);
    }

}
