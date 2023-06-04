package at.hypercrawler.crawlerservice.crawler.domain.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.*;

@Node
@Value
@AllArgsConstructor
public class PageNode {
    @Id
    String url;

    @NonFinal
    List<String> indices;

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

    @NonFinal
    @Relationship(type = "LINKS_TO", direction = Relationship.Direction.OUTGOING)
    Set<PageNode> linksTo;

    @NonFinal
    @Version
    Long version;

    public PageNode(String address, UUID crawlerId, Integer value, Instant lastModifiedDateOfPage, MediaType type, Long contentLength, String body) {
        this(address, new ArrayList<>(), crawlerId, value, lastModifiedDateOfPage, type.toString(), contentLength, body, Instant.now(), Instant.now(), new HashSet<>(), 0L);
    }

    public PageNode(String url, UUID crawlerId) {
        this(url, new ArrayList<>(), crawlerId, null, null, null, null, null, Instant.now(), Instant.now(), new HashSet<>(), 0L);
    }

    public void addPageNode(PageNode pageNode) {
        if (this.linksTo == null) this.linksTo = new HashSet<>();
        this.linksTo.add(pageNode);
    }

    public void addIndex(String s) {
        if (this.indices == null) this.indices = new ArrayList<>();
        this.indices.add(s);
    }

    public MediaType getContentType() {
        if (this.contentType == null) return null;
        return MediaType.valueOf(this.contentType);
    }

}
