package com.baeldung.crawler4j.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.baeldung.common.GlobalConstants;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlerForFindingReadmeURLs extends BaseCrawler {
    
    private final static Pattern FILTER_ADDITIONAL_FILE_EXTENTIONS = Pattern.compile(".*(\\.(java))$");
    private final static Pattern FILTERS_ADDITIONAL_DIRECTORIES = Pattern.compile(".*(\\/test\\/).*");
    private List<String> discoveredURLs = new ArrayList<>();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String referringPageURL = referringPage.getWebURL().getURL();
        // @formatter:off
        return super.commonPredicate(href, referringPageURL)
                && !referringPageURL.toLowerCase().contains(GlobalConstants.README_FILE_NAME_LOWERCASE)
                && !FILTER_ADDITIONAL_FILE_EXTENTIONS.matcher(href).matches()
                && !FILTERS_ADDITIONAL_DIRECTORIES.matcher(href).matches();
        // @formatter:on
    }

    @Override
    public void visit(Page page) {
        if (page.getWebURL().getURL().toLowerCase().endsWith(GlobalConstants.README_FILE_NAME_LOWERCASE)) {           
            this.discoveredURLs.add(page.getWebURL().getURL());
        }
    }
    
    @Override
    public List<String> getMyLocalData() {
        return discoveredURLs;
    }

}
