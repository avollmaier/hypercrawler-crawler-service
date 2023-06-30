package at.hypercrawler.crawlerservice.crawler.domain.service.post;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.crawler.domain.util.RegexUtil;
import at.hypercrawler.crawlerservice.manager.CrawlerAction;
import at.hypercrawler.crawlerservice.manager.SupportedContentMediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ActionHandler {

    public PageNode handleActions(PageNode pageNode, List<CrawlerAction> actions, String indexPrefix) {
        if (pageNode == null || actions == null || indexPrefix == null) {
            log.warn("PageNode, actions or indexPrefix was null. Returning pageNode without any changes.");
            return pageNode;
        }

        for (CrawlerAction action : actions) {
            if (matchesAction(action, pageNode)) {
                pageNode.addIndex(indexPrefix + action.indexName());
            } else {
                log.warn("Action {} did not match for pageNode with parent: {}", action, pageNode.getUrl());
            }
        }
        return pageNode;
    }

    private boolean matchesAction(CrawlerAction action, PageNode pageNode) {
        Pattern selectorsToMatch = RegexUtil.combineRegex(action.selectorsToMatch());
        Pattern pathsToMatch = RegexUtil.combineRegex(action.pathsToMatch());

        log.info("Checking if preconfigured action matches address {} with content type {}", pageNode.getUrl(), pageNode.getContentType());

        if (checkPathToMatch(pageNode, pathsToMatch)) return true;
        if (checkSelectorToMatch(pageNode, selectorsToMatch)) return true;
        if (checkContentTypeToMatch(pageNode, action.contentTypesToMatch())) return true;

        return false;
    }

    private static boolean checkContentTypeToMatch(PageNode pageNode, List<SupportedContentMediaType> contentTypesToMatch) {
        if (contentTypesToMatch == null || contentTypesToMatch.isEmpty() || pageNode == null || pageNode.getContentType() == null) {
            return false;
        }

        if (contentTypesToMatch.stream().anyMatch(contentType -> pageNode.getContentType().equals(contentType.getFormat()))) {
            log.info("File type matches with contentTypesToMatch {} and content type {}", contentTypesToMatch, pageNode.getContentType());
            return true;
        }
        return false;
    }

    private static boolean checkPathToMatch(PageNode pageNode, Pattern pathsToMatch) {
        if (pathsToMatch == null || pageNode == null || pageNode.getUrl() == null) {
            return false;
        }


        if (pageNode.getUrl().matches(pathsToMatch.pattern())) {
            log.info("Path matches with regex {} and address {}", pathsToMatch, pageNode.getUrl());
            return true;
        }

        return false;
    }

    private static boolean checkSelectorToMatch(PageNode pageNode, Pattern selectorsToMatch) {
        if (selectorsToMatch == null || pageNode == null || pageNode.getContent() == null) {
            return false;
        }

        if (Pattern.compile(selectorsToMatch.pattern()).matcher(pageNode.getContent()).find()) {
            log.info("Selector matches with regex {} and content {}", selectorsToMatch, pageNode.getContent());
            return true;
        }

        return false;
    }
}
