package at.hypercrawler.crawlerservice.crawler.domain.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.*;

@Node
@Data
public class PageNode {
    @Id
    String url;

    List<String> indices;

    UUID crawlerId;

    Integer responseCode;

    Long lastModified;

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

    public PageNode(String address, UUID crawlerId, Integer value, Long lastModified, String type, Long contentLength, String body) {
        this.url = address;
        this.crawlerId = crawlerId;
        this.responseCode = value;
        this.lastModified = lastModified;
        this.contentType = type;
        this.contentLength = contentLength;
        this.content = body;
    }

    public void addPageNode(PageNode pageNode) {
        if (this.linksTo == null) this.linksTo = new HashSet<>();
        this.linksTo.add(pageNode);
    }

    public void addIndex(String s) {
        if (this.indices == null) this.indices = new ArrayList<>();
        this.indices.add(s);
    }
}
