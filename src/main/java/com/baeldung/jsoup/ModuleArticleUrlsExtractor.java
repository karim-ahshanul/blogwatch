package com.baeldung.jsoup;

import com.baeldung.common.ConsoleColors;
import com.google.common.annotations.VisibleForTesting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ModuleArticleUrlsExtractor {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private static final int TIMEOUT = 10000;
    private static final String LINK_TAG = "a";
    private static final String LINK_ATTRIBUTE = "href";
    private static final String README_FILE_NAME = "README.md";
    private static final String ARTICLE_LINK_PATTERN = "https?://www\\.baeldung\\.com/.*";

    public Set<URL> findArticleUrlsInModules(Collection<URL> moduleUrls) {
        return moduleUrls.stream()
          .map(this::readmeUrl)
          .map(this::parseDocument)
          .flatMap(this::selectArticleLinks)
          .map(this::toUrl)
          .collect(Collectors.toSet());
    }

    private URL readmeUrl(URL url) {
        String readmeFileUrl = url.toString() + "/" + README_FILE_NAME;
        try {
            return new URL(readmeFileUrl);
        } catch (MalformedURLException e) {
            
            throw new IllegalStateException("The README file URL isn't right: " + readmeFileUrl);
        }
    }

    @VisibleForTesting
    Document parseDocument(URL url) {
        try {
            return Jsoup.parse(url, TIMEOUT);
        } catch (IOException e) {
            logger.error(ConsoleColors.redBoldMessage("Error while parsing README for:{}"), url);
            throw new IllegalStateException("A problem occurred while parsing HTML document at URL " + url, e);
        }
    }

    private Stream<Element> selectArticleLinks(Document document) {
        return document
          .getElementsByTag(LINK_TAG)
          .stream()
          .filter(element -> element.attr(LINK_ATTRIBUTE).matches(ARTICLE_LINK_PATTERN));
    }

    private URL toUrl(Element element) {
        try {
            return new URL(element.attr(LINK_ATTRIBUTE));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("An error occurred while parsing URL " + element.attr(LINK_ATTRIBUTE), e);
        }
    }
}
