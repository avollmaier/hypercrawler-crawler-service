package at.hypercrawler.crawlerservice.manager;

import lombok.Getter;

@Getter
public enum SupportedFileType {
    HTML("html"), PDF("pdf"), TXT("txt");

    private final String format;

    SupportedFileType(String format) {
        this.format = format;
    }
}
