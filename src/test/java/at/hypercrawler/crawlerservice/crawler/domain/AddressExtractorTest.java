package at.hypercrawler.crawlerservice.crawler.domain;

import at.hypercrawler.crawlerservice.crawler.domain.service.crawl.AddressExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AddressExtractorTest {

    private AddressExtractor addressExtractor;

    @BeforeEach
    public void setup() {
        addressExtractor = new AddressExtractor();
    }

    @Test
    public void whenEmptyString_resultIsEmpty() {
        String input = "";
        List<String> result = addressExtractor.apply(input);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void whenStringWithNoUrls_resultIsEmpty() {
        String input = "This is a sample text without any URLs.";
        List<String> result = addressExtractor.apply(input);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void whenStringWithOneUrl_resultHasOneUrl() {
        String input = "Check out this website: https://example.com";
        List<String> result = addressExtractor.apply(input);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("https://example.com", result.get(0));
    }

    @Test
    public void whenStringWithTwoUrls_resultHasTwoUrls() {
        String input = "Visit these sites: www.google.com and ftp://example.org";
        List<String> result = addressExtractor.apply(input);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("www.google.com", result.get(0));
        Assertions.assertEquals("ftp://example.org", result.get(1));
    }

    @Test
    public void whenStringWithOneSpectialUrl_resultHasOneUrls() {
        String input = "Here are some URLs: http://www.example.com/path?param=value#fragment";
        List<String> result = addressExtractor.apply(input);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("http://www.example.com/path?param=value#fragment", result.get(0));
    }
}
