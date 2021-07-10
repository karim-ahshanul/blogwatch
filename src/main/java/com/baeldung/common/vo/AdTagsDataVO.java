package com.baeldung.common.vo;

import java.util.List;

public class AdTagsDataVO {
    private String url;
    private List<String> adTags;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public List<String> getAdTags() {
        return adTags;
    }
    public void setAdTags(List<String> adTags) {
        this.adTags = adTags;
    }
    
}
