package at.hypercrawler.crawlerservice.crawler.domain;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.service.post.ActionHandler;
import at.hypercrawler.crawlerservice.manager.CrawlerAction;
import at.hypercrawler.crawlerservice.manager.SupportedContentMediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ActionHandlerTest {

    private ActionHandler actionHandler;

    @BeforeEach
    public void setUp() {
        actionHandler = new ActionHandler();
    }

    @Test
    public void whenPageNodeMatchesPathPattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.singletonList(".*example.com.*"), Collections.emptyList(), Collections.emptyList());
        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID());

        PageNode result = actionHandler.handleActions(pageNode, Collections.singletonList(action), "test_");

        assertEquals(1, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
    }

    //test multiple path matcher actions
    @Test
    public void whenPageNodeMatchesMultiplePathPattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.singletonList(".*example.com.*"), Collections.emptyList(), Collections.emptyList());
        CrawlerAction action2 = new CrawlerAction("action2", Collections.singletonList(".*com.*"), Collections.emptyList(), Collections.emptyList());
        CrawlerAction action3 = new CrawlerAction("action2", null, Collections.emptyList(), Collections.emptyList());

        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID());

        PageNode result = actionHandler.handleActions(pageNode, Arrays.asList(action, action2, action3), "test_");

        assertEquals(2, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
        assertEquals("test_action2", result.getIndices().get(1));
    }

    //negative test
    @Test
    public void whenPageNodeDoesNotMatchPathPattern_thenMatchesActionReturnsFalse() {
        CrawlerAction action = new CrawlerAction("action1", Collections.singletonList(".*example.com.*"), Collections.emptyList(), Collections.emptyList());
        PageNode pageNode = new PageNode("https://example.at", UUID.randomUUID());

        PageNode result = actionHandler.handleActions(pageNode, Collections.singletonList(action), "test_");

        assertEquals(0, result.getIndices().size());
    }

    @Test
    public void whenPageNodeIsNull_thenMatchesActionReturnsFalse() {
        CrawlerAction action = new CrawlerAction("action1", Collections.singletonList(".*example.com.*"), Collections.emptyList(), Collections.emptyList());

        PageNode result = actionHandler.handleActions(null, Collections.singletonList(action), "test_");

        assertNull(result);
    }

    @Test
    public void whenPageNodeMatchesContentTypePattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(SupportedContentMediaType.HTML));
        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID(), 342, Instant.now(), MediaType.TEXT_HTML, 1243L, "test");

        PageNode result = actionHandler.handleActions(pageNode, Collections.singletonList(action), "test_");

        assertEquals(1, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
    }

    @Test
    public void whenPageNodeMatchesMultipleContentTypePattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(SupportedContentMediaType.HTML));
        CrawlerAction action2 = new CrawlerAction("action2", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(SupportedContentMediaType.HTML));
        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID(), 342, Instant.now(), MediaType.TEXT_HTML, 1243L, "test");

        PageNode result = actionHandler.handleActions(pageNode, Arrays.asList(action, action2), "test_");

        assertEquals(2, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
        assertEquals("test_action2", result.getIndices().get(1));
    }

    @Test
    public void whenPageNodeDoesNotMatchContentTypePattern_thenMatchesActionReturnsFalse() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.emptyList(), Collections.singletonList(SupportedContentMediaType.HTML));
        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID(), 342, Instant.now(), MediaType.APPLICATION_PDF, 1243L, "test");

        PageNode result = actionHandler.handleActions(pageNode, List.of(action), "test_");

        assertEquals(0, result.getIndices().size());
    }

    // the same 3 tests as above, but with selector matching
    @Test
    public void whenPageNodeMatchesSelectorPattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.singletonList(".*test1*"), Collections.emptyList());

        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID(), 342, Instant.now(), MediaType.TEXT_HTML, 1243L, "test1");

        PageNode result = actionHandler.handleActions(pageNode, Collections.singletonList(action), "test_");

        assertEquals(1, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
    }

    @Test
    public void whenPageNodeMatchesMultipleSelectorPattern_thenMatchesActionReturnsTrue() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.singletonList(".*test1*"), Collections.emptyList());
        CrawlerAction action2 = new CrawlerAction("action2", Collections.emptyList(), Collections.singletonList(".*test2*"), Collections.emptyList());
        PageNode pageNode = new PageNode("https://example.com", UUID.randomUUID(), 342, Instant.now(), MediaType.TEXT_HTML, 1243L, "test");

        PageNode result = actionHandler.handleActions(pageNode, Arrays.asList(action, action2), "test_");

        assertEquals(2, result.getIndices().size());
        assertEquals("test_action1", result.getIndices().get(0));
        assertEquals("test_action2", result.getIndices().get(1));
    }

    @Test
    public void whenPageNodeDoesNotMatchSelectorPattern_thenMatchesActionReturnsFalse() {
        CrawlerAction action = new CrawlerAction("action1", Collections.emptyList(), Collections.singletonList(".*test1.*"), Collections.emptyList());
        PageNode pageNode = new PageNode("https://example.at", UUID.randomUUID(), 342, Instant.now(), MediaType.TEXT_HTML, 1243L, "test");

        PageNode result = actionHandler.handleActions(pageNode, Collections.singletonList(action), "test_");

        assertEquals(0, result.getIndices().size());
    }

}
