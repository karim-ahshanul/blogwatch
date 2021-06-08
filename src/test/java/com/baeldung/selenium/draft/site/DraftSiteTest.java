package com.baeldung.selenium.draft.site;

import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.selenium.common.BaseUISeleniumTest;

@ExtendWith(TestMetricsExtension.class)
public class DraftSiteTest extends BaseUISeleniumTest {

	@Value("${draft.site.username:auser}")
	private String draftSiteUserName;

	@Value("${draft.site.password:apassword}")
	private String draftSitePasswrod;

	@BeforeEach
	public final void loginToDraftSite() {

		page.setUrl("http://drafts.baeldung.com/wp-admin");
		page.loadUrl();
		page.getWebDriver().findElement(By.id("user_login")).sendKeys(this.draftSiteUserName);
		page.getWebDriver().findElement(By.id("user_pass")).sendKeys(this.draftSitePasswrod);

		page.getWebDriver().findElement(By.id("wp-submit")).click();
		assertTrue("automation api".equals(page.getDisplayNameOfLoggedInUser()), "Couldn't login to Draft Site");

	}

	@ParameterizedTest(name = " {displayName} - on {0}")
	@MethodSource("com.baeldung.utility.TestUtils#gaCodeTestDataProviderForDraftSite")
	@Tag(GlobalConstants.TAG_DAILY)
	public final void givenAGoogleAnalyticsEnabledPageOnTheDraftSite_whenAnalysingThePageSource_thenItHasTrackingCode(String url) {

		page.setUrl(url);
		page.loadUrl();

		assertTrue(page.getAnalyticsScriptCount() == 1, "GA script count is not equal to 1 on:" + url);
	}
}
