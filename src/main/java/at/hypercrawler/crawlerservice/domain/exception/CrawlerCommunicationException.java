package at.hypercrawler.crawlerservice.domain.exception;

import java.net.URL;

public class CrawlerCommunicationException extends RuntimeException {
    public CrawlerCommunicationException(URL address, Exception e) {
        super("Error while communication with the address: " + address, e);
    }
}
