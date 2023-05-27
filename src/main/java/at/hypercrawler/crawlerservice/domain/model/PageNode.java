package at.hypercrawler.crawlerservice.domain.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Node
@Data
public class PageNode {
    @Id
    String url;

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

    public PageNode(String url, Integer responseCode, Duration responseTime, String contentType, Long contentLength, String content) {
        this.url = url;
        this.responseCode = responseCode;
        this.responseTime = responseTime;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.content = content;
    }

    public void addPageNode(PageNode pageNode) {
        this.linksTo.add(pageNode);
    }

}
