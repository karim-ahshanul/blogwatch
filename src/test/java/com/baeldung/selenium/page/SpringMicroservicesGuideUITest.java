package com.baeldung.selenium.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.SpringMicroservicesGuidePage;

import io.restassured.RestAssured;

@ExtendWith(TestMetricsExtension.class)
public final class SpringMicroservicesGuideUITest extends BaseUISeleniumTest {

    @Autowired
    private SpringMicroservicesGuidePage springMicroservicesGuidePage;

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    @Tag(GlobalConstants.TAG_SITE_SMOKE_TEST)
    public final void givenOnTheMicroservicesGuidePage_whenOptinPopupIsLoaded_thenItContainsImages() {
        this.springMicroservicesGuidePage.loadUrl();

        springMicroservicesGuidePage.clickAccessTheGuideButton();

        List<WebElement> elements = springMicroservicesGuidePage.findImages();
        elements.forEach(element -> {
            assertEquals(200, RestAssured.given().head(element.getAttribute("src")).getStatusCode());
        });

    }

}
