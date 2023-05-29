package at.hypercrawler.crawlerservice.crawler.domain.service.postprocess;

import at.hypercrawler.crawlerservice.crawler.domain.model.PageNode;
import at.hypercrawler.crawlerservice.manager.CrawlerAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ActionHandler {

    private final GlobConverter globConverter;

    public ActionHandler(GlobConverter globConverter) {
        this.globConverter = globConverter;
    }

    public PageNode handleActions(PageNode pageNode, List<CrawlerAction> actions, String indexPrefix) {
        for (CrawlerAction action : actions) {
            if (matchesAction(action, pageNode)) {
                pageNode.addIndex(indexPrefix + action.indexName());
            } else {
                log.error("Action {} did not match for pageNode {}", action, pageNode);
            }
        }
        return pageNode;
    }

    private boolean matchesAction(CrawlerAction action, PageNode pageNode) {
        String pathsToMatchRegex = globConverter.convertGlobsToRegex(action.pathsToMatch());
        String selectorsToMatchRegex = globConverter.convertGlobsToRegex(action.selectorsToMatch());
        Matcher matcher = Pattern.compile(selectorsToMatchRegex).matcher(pageNode.getUrl());

        if (matcher.find()) {
            return true;
        }

        log.info("Checking if {} matches {}", pageNode.getUrl(), pathsToMatchRegex);
        if (pathsToMatchRegex.matches(pageNode.getUrl())) {
            return true;
        }

        log.info("Checking if {} matches {}", pageNode.getContentType(), action.fileTypesToMatch());
        return action.fileTypesToMatch().stream().anyMatch(fileType -> pageNode.getContentType().equals(fileType.getFormat()));
    }
}
