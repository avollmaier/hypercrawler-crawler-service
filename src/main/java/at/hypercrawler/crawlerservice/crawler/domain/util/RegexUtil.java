package at.hypercrawler.crawlerservice.crawler.domain.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class RegexUtil {

    public static Pattern combineRegex(List<String> regexes) {
        if (regexes == null || regexes.isEmpty()) {
            log.info("No regexes found, returning regex that matches nothing");
            return Pattern.compile("a^");
        }

        String combinedRegex = regexes.stream().reduce((s, s2) -> s + "|" + s2).orElse("");
        log.info("Combined regex {}", combinedRegex);
        return Pattern.compile(combinedRegex);
    }
}
