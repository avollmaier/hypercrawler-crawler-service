package at.hypercrawler.crawlerservice.crawler.domain;

import at.hypercrawler.crawlerservice.crawler.domain.util.RegexUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegexUtilTest {

    @Test
    public void whenCombineRegexWithNullList_thenReturnEmptyPattern() {
        List<String> regexes = null;
        Pattern result = RegexUtil.combineRegex(regexes);

        assertEquals(Pattern.compile("a^").pattern(), result.pattern());
    }

    @Test
    public void whenCombineRegexWithEmptyList_thenReturnEmptyPattern() {
        List<String> regexes = Collections.emptyList();
        Pattern result = RegexUtil.combineRegex(regexes);

        assertEquals(Pattern.compile("a^").pattern(), result.pattern());
    }

    @Test
    public void whenCombineRegexWithNonEmptyList_thenCombineRegexesWithOROperator() {
        List<String> regexes = Arrays.asList("regex1", "regex2", "regex3");
        Pattern result = RegexUtil.combineRegex(regexes);

        assertEquals(Pattern.compile("regex1|regex2|regex3").pattern(), result.pattern());
    }
}

