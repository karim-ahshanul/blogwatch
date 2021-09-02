package com.baeldung.selenium.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.HomePageDriver;

import io.restassured.RestAssured;
import io.restassured.response.Response;

@ExtendWith(TestMetricsExtension.class)
public final class HomePageUITest extends BaseUISeleniumTest {

    @Autowired
    private HomePageDriver homePageDriver;

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    @Tag(GlobalConstants.TAG_SITE_SMOKE_TEST)
    public final void givenOnTheHomePage_whenPageLoads_thenJavaWeeklyLinksMatchWithTheLinkText() {
        homePageDriver.loadUrl();
        List<WebElement> javaWeeklyElements = this.homePageDriver.getAllJavaWeeklyIssueLinkElements();

        String expectedLink;
        String issueNumber;
        for (WebElement webElement : javaWeeklyElements) {
            issueNumber = webElement.getText().replaceAll("\\D+", "");
            if (issueNumber.length() > 0) {
                expectedLink = (this.homePageDriver.getUrl() + "/java-weekly-") + issueNumber;
                logger.debug("expectedLink-->" + expectedLink);
                logger.debug("actual  Link-->" + webElement.getAttribute("href"));

                assertTrue(expectedLink.equals(webElement.getAttribute("href").toString()));
            }
        }
    }    

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnTheHomePageUrlWithoutWWWPrefix_whenUrlIsHit_thenItRedirectsToWWW() {
        Response response = RestAssured.given().redirects().follow(false).head(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITHOUT_WWW_PREFIX);

        assertEquals(301, response.getStatusCode());
        assertEquals(GlobalConstants.BAELDUNG_HOME_PAGE_URL_WITH_WWW_PREFIX, response.getHeader("Location").replaceAll("/$", ""));
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnTheHomePage_whenPageLoads_thenItHasOneAboutMenuInTheFooter() {
        homePageDriver.loadUrl();
        assertTrue(homePageDriver.findAboutMenuInThePageFooter().size() == 1);
    }

    @Test
    @Tag("screenshot-test")
    public final void screenShotTest() throws IOException, InterruptedException {

        homePageDriver.getWebDriver().manage().window().setSize(new Dimension(1920, 1080));
        // homePageDriver.getWebDriver().manage().window().maximize();

        homePageDriver.loadUrl();
        Thread.sleep(5000);

        File scrFile = ((TakesScreenshot) homePageDriver.getWebDriver()).getScreenshotAs(OutputType.FILE);

        // Now you can do whatever you need to do with it, for example copy somewhere
        FileUtils.copyFile(scrFile, new File("/tmp/screenshots/screenshot.png"));
        // assertTrue(homePageDriver.findCategoriesContainerInThePageFooter().isDisplayed());
    }

}
