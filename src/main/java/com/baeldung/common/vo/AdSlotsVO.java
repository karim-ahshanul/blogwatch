package com.baeldung.common.vo;

import java.util.List;

public class AdSlotsVO {
    private String url;
    private List<String> slotIds;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public List<String> getSlotIds() {
        return slotIds;
    }
    public void setSlotIds(List<String> slotIds) {
        this.slotIds = slotIds;
    }        
}
