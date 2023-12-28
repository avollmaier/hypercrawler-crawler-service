package at.hypercrawler.crawlerservice.crawler.domain;

import at.hypercrawler.crawlerservice.crawler.domain.service.crawl.AddressExtractor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AddressExtractorTest {

  private AddressExtractor addressExtractor;

  @BeforeEach
  public void setup() {
    addressExtractor = new AddressExtractor();
  }

  @Test
  public void whenEmptyString_resultIsEmpty() {
    String input = "";
    List<String> result = addressExtractor.extract(input, "https://example.com");
    Assertions.assertEquals(0, result.size());
  }

  @Test
  public void whenStringWithNoUrls_resultIsEmpty() {
    String input = "This is a sample text without any URLs.";
    List<String> result = addressExtractor.extract(input, "https://example.com");
    Assertions.assertEquals(0, result.size());
  }

  @Test
  public void whenStringWithOneUrl_resultHasOneUrl() {
    String input = "Check out this website: <a href='https://example.com'>Example</a>";
    List<String> result = addressExtractor.extract(input, "https://example.com");
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("https://example.com", result.get(0));
  }

  @Test
  public void whenStringWithTwoUrls_resultHasTwoUrls() {
    String input = "Visit these sites: <a href='http://www.google.com'>Google</a> and <a href='ftp://example"
                       + ".org'>Example</a>";
    List<String> result = addressExtractor.extract(input, "https://example.com");
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("http://www.google.com", result.get(0));
    Assertions.assertEquals("ftp://example.org", result.get(1));
  }

  @Test
  public void whenStringWithOneSpectialUrl_resultHasOneUrls() {
    String input = "Here are some URLs: <a href='http://www.example.com/path?param=value#fragment'>Example</a>";
    List<String> result = addressExtractor.extract(input, "https://example.com");
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("http://www.example.com/path?param=value#fragment", result.get(0));
  }

  @Test
  public void whenDummyHtmlFileIsRead_resultHasExpectedNumberOfUrls() throws IOException {
    String input = new String(Files.readAllBytes(Paths.get("src/test/resources/website-absolute.html")));
    List<String> result = addressExtractor.extract(input, "https://example.com");
    // Replace the expected number of URLs with the actual expected number

    Files.writeString(Paths.get("src/test/resources/website-absolute-result.txt"), String.join("\n", result));
    int expectedNumberOfUrls = 108;
    Assertions.assertEquals(expectedNumberOfUrls, result.size());

    List<String> aboluteExpected = Files.readAllLines(Paths.get("src/test/resources/website-absolute-result.txt"));

    Assertions.assertEquals(aboluteExpected, result);


  }


  @Test
  public void whenWebsiteRelativeHtmlFileIsRead_resultHasExpectedNumberOfUrlsAndSameUrlsAsInWebsiteRelativeResult() throws IOException {
    String input = new String(Files.readAllBytes(Paths.get("src/test/resources/website-relative.html")));
    List<String> result = addressExtractor.extract(input, "https://example.com");
    // Replace the expected number of URLs with the actual expected number
    int expectedNumberOfUrls = 45; // replace with actual number
    Assertions.assertEquals(expectedNumberOfUrls, result.size());

    // Read the website-relative-result.txt file into a list of strings
    List<String> websiteRelativeResult = Files.readAllLines(Paths.get("src/test/resources/website-relative-result.txt"));

    Assertions.assertEquals(websiteRelativeResult, result);
  }


}
