package at.hypercrawler.crawlerservice.crawler.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.*;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageNode {
    @Id
    String url;

    @Builder.Default
    List<String> indices = new ArrayList<>();

    UUID crawlerId;

    Integer responseCode;

    Instant lastModifiedDateOfPage;

    String contentType;

    Long contentLength;

    String content;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    Set<PageNode> linksTo = new HashSet<>();

    @NonFinal
    Long version;

    public void addPageNode(PageNode pageNode) {
        this.linksTo.add(pageNode);
    }

    public void addIndex(String s) {
        this.indices.add(s);
    }

    public MediaType getContentType() {
        if (this.contentType == null) return null;
        return MediaType.valueOf(this.contentType);
    }

}
