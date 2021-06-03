package com.baeldung.selenium.page;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;
import com.baeldung.site.OptInPageDriver;
import com.baeldung.utility.TestUtils;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(TestMetricsExtension.class)
public class OptInPopupUITest extends BaseUISeleniumTest {
    @Autowired
    private OptInPageDriver optInPageDriver;

    @Value("${givenAPageWithOptins_whenThePopupsAreOpened_thenTheOptInsPopupsWorkFine.team-optin-urls}")
    private String[] optInUrls;

    @Value("${givenAPageWithOptins_whenThePopupsAreOpened_thenTheOptInsPopupsWorkFine.team-optin-test-retry}")
    private int retry;

    int retryCount = 0;

    @BeforeEach
    public void loadNewWindow() throws IOException {
        page.openNewWindow();
        retryCount = 0;
    }

    @ParameterizedTest(name = " {displayName} - Test opt-in popups on {0} ")
    @MethodSource("optInPopusTestDataProvider")
    @Tag("optinPopsTest")
    public final void givenAPageWithOptins_whenThePopupsAreOpened_thenTheOptInsPopupsWorkFine(String optInPagUrl) {
        optInPageDriver.setUrl(optInPagUrl);
        try {
            optInPageDriver.loadUrl();
            optInPageDriver.clickOnGetAccessLinkforSmallTeam();
            assertTrue(optInPageDriver.theFirstNameInputFieldIsDisplayed(), "Problem with opt-in pop-up for small team");
            optInPageDriver.closePopupOnRwSTeamOptInPage();
            
            optInPageDriver.clickOnGetAccessLinkforMediumTeam();
            assertTrue(optInPageDriver.theFirstNameInputFieldIsDisplayed(), "Problem with opt-in pop-up for medium team");
            optInPageDriver.closePopupOnRwSTeamOptInPage();
            
            optInPageDriver.clickOnGetAccessLinkforLargeTeam();
            assertTrue(optInPageDriver.theFirstNameInputFieldIsDisplayed(), "Problem with opt-in pop-up for Large team");
            optInPageDriver.closePopupOnRwSTeamOptInPage();

        } catch (Exception e) {
            TestUtils.takeScreenShot(optInPageDriver.getWebDriver());
            if (retry == retryCount) {
                logger.debug(retry + " retries completed.");
                logger.error(e.getMessage());
                fail(e.getMessage());
            } else {
                retryCount++;
                logger.error(e.getMessage());
                givenAPageWithOptins_whenThePopupsAreOpened_thenTheOptInsPopupsWorkFine(optInPagUrl);

            }
        }

    }

    public Stream<Arguments> optInPopusTestDataProvider() {
        return Arrays.stream(optInUrls).map(url -> Arguments.of(url));
    }

}
