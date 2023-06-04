package at.hypercrawler.crawlerservice.manager;

import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public enum SupportedContentMediaType {
    HTML(MediaType.TEXT_HTML), PDF(MediaType.APPLICATION_PDF), TXT(MediaType.TEXT_PLAIN);

    private final MediaType format;

    SupportedContentMediaType(MediaType format) {
        this.format = format;
    }
}
