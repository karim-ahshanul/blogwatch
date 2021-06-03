package com.baeldung.crawler4j.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TutorialsRepoCrawlerController extends BaseCrawlController {

    @Value("#{'${TutorialsRepoCrawlerController.repo-tutorials}'.split(',')}")
    private List<String> urls;    

    public List<Object> getDiscoveredURLs() {
        return this.getCrawlersLocalData();
    }

    @PostConstruct
    public void setSeedURLs() {
        this.setSeedURLs(urls);
    }

}
