package com.baeldung.site;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SpringMicroservicesGuidePage extends BlogBaseDriver {

    public void clickAccessTheGuideButton() { 
        try {
        this.getWebDriver().findElement(By.xpath("//a//span[contains(text(), 'ACCESS THIS GUIDE')]")).click();
        }catch(ElementNotVisibleException e ) {
            this.acceptCookie();
        }
    }

    public List<WebElement> findImages() {
        return this.getWebDriver().findElements(By.xpath("//*[@id='tve_editor']//img"));
    }

    @Override
    @Value("${site.guide.spring.microservices}")
    protected void setUrl(String pageURL) {
        this.url = this.getBaseURL() + pageURL;
    }

}
