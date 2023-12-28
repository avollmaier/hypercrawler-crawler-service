package at.hypercrawler.crawlerservice.crawler.domain.service.crawl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class AddressExtractor {
    public List<String> extract(String htmlContent, String baseUrl) {
        List<String> containedUrls = new ArrayList<>();

        Document doc = Jsoup.parse(htmlContent, baseUrl);
        Elements links = doc.select("a[href]");

        for (Element link : links) {
            String url = link.attr("abs:href");
            if (isResolvable(url)) {
                containedUrls.add(url);
            }
        }

        return containedUrls;
    }

    private boolean isResolvable(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}