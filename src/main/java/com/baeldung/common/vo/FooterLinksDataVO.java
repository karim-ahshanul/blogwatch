package com.baeldung.common.vo;

import java.util.List;

public class FooterLinksDataVO {
    private List<String> urls;
    private String footerTag;
    private List<Link> footerLinks;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getFooterTag() {
        return footerTag;
    }

    public void setFooterTag(String footerTag) {
        this.footerTag = footerTag;
    }

    public List<Link> getFooterLinks() {
        return footerLinks;
    }

    public void setFooterLinks(List<Link> footerLinks) {
        this.footerLinks = footerLinks;
    }

    public static class Link {
        private String anchorText;
        private String anchorLink;
        private FooterLinkCategory linkCategory;

        public String getAnchorText() {
            return anchorText;
        }

        public void setAnchorTest(String anchorText) {
            this.anchorText = anchorText;
        }

        public String getAnchorLink() {
            return anchorLink;
        }

        public void setAnchorLink(String anchorLink) {
            this.anchorLink = anchorLink;
        }

        public FooterLinkCategory getLinkCategory() {
            return linkCategory;
        }

        public void setLinkCategory(FooterLinkCategory linkCategory) {
            this.linkCategory = linkCategory;
        }

    }

    public static enum FooterLinkCategory {
        WRITE_FOR_BAELDUNG, NORMAL;

        

    }
}
