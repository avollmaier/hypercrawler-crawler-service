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

@Builder
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageNode {

    @Id
    @GeneratedValue
    Long id;

    String baseUrl;

    Integer responseCode;

    Duration responseTime;

    MediaType contentType;

    Long contentLength;

    String content;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    Set<PageNode> linksTo;

    @CreatedDate
    Instant createdAt;

    @LastModifiedDate
    Instant updatedAt;

    public PageNode(String baseUrl, Integer responseCode, Duration responseTime, MediaType  contentType, Long contentLength, String content) {
        this.baseUrl = baseUrl;
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
