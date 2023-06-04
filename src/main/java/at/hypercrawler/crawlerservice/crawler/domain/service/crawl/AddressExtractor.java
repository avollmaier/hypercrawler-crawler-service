package at.hypercrawler.crawlerservice.crawler.domain.service.crawl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AddressExtractor implements Function<String, List<String>> {
    @Override
    public List<String> apply(String s) {
        List<String> containedUrls = new ArrayList<>();

        String urlRegex = "(?:(?:https?|ftp):\\/\\/|www\\.)" +
                "[\\w\\d\\-_]+(?:\\.[\\w\\d\\-_]+)" +
                "[\\w\\d\\-\\.,@?^=%&amp;:/~\\+#]*" +
                "[\\w\\d\\-\\@?^=%&amp;/~\\+#]";

        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(s);

        while (urlMatcher.find()) {
            containedUrls
                    .add(s.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        log.info("Found {} urls in {} characters", containedUrls.size(), s.length());
        return containedUrls;
    }
}
