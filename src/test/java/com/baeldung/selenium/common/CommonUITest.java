package com.baeldung.selenium.common;

import static com.baeldung.common.ConsoleColors.*;
import static com.baeldung.common.GlobalConstants.TestMetricTypes.FAILED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.ConsoleColors;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.common.vo.AnchorLinksTestDataVO;
import com.baeldung.common.vo.CoursePurchaseLinksVO.PurchaseLink;
import com.baeldung.common.vo.EventTrackingVO;
import com.baeldung.common.vo.FooterLinksDataVO;
import com.baeldung.common.vo.FooterLinksDataVO.FooterLinkCategory;
import com.baeldung.common.vo.LinkVO;
import com.baeldung.crawler4j.crawler.CrawlerForFindingReadmeURLs;
import com.baeldung.filevisitor.ModuleAlignmentValidatorFileVisitor;
import com.baeldung.filevisitor.TutorialsParentModuleFinderFileVisitor;
import com.baeldung.utility.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.Retryer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;

@ExtendWith(TestMetricsExtension.class)
public class CommonUITest extends BaseUISeleniumTest {

    @Value("${GivenAGitHubModuleReadme_whenAnalysingTheReadme_thentheReadmeDoesNotLikTooManyArticles.limit-for-readme-having-articles}")
    private int limitForReadmeHavingArticles;

    @Value("${GivenAGitHubModuleReadme_whenAnalysingTheReadme_thentheReadmeDoesNotLikTooManyArticles.limit-for-spring-realted-readme-having-articles}")
    private int limitForSpringRelatedReadmeHavingArticles;

    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.time-out-for-200OK-test}")
    private int timeOutFor200OKTest;

    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.retries-for-200OK-test}")
    private int retriesFor200OKTest;
    
    @Value("${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.mode-for-200OK-test}")
    private String modeFor200OKTest;

    @Value("${givenTheBaeldungRSSFeed_whenAnalysingFeed_thenItIsUptoDate.rss-feed-compare-days}")
    private int rssFeedShouldNotbeOlderThanDays;

    @Value("#{'${givenAListOfUrls_whenAUrlLoads_thenItReturns200OK.site-status-check-url-file-names:course-pages.txt}'.split(',')}")
    private List<String> pageStausCheckUrlFileNames;
    
    @Value("${givenURLsWithFooterLinks_whenAnaysingFooterLinks_thenAnchorTextAndAnchorLinksExist.verify-write-for-baeldung-footer-link}")
    private boolean verifyWriteForBaeldungFooterLink;     
    
    @Value("${givenAPage_whenThePageLoads_thenNoPopupAppearsOnThePage.time-to-wait-for-popup}")
    private int timeToWaitForPopup;
    
    @Value("${givenAnArtifactId_thenListAllChildModules.parent-artifact-id}")
    private String parentArtifactId;
    
    @Value("${redownload-repo}")
    private String redownloadTutorialsRepo;

    @Autowired
    ObjectMapper objectMapper;
    
    RestAssuredConfig restAssuredConfig = TestUtils.getRestAssuredCustomConfig(5000);

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenThePagesWithBlankTitle_whenPageLoads_thenItDoesNotContainNotitleText() {
        GlobalConstants.PAGES_WITH_BLANK_TITLE.forEach(url -> {
            page.setUrl(page.getBaseURL() + url);

            page.loadUrl();

            assertFalse(page.getCountOfElementsWithNotitleText() > 0, "page found with 'No Title' in body-->" + url);
        });
    }

    @Test
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public final void givenAListOfUrls_whenAUrlLoads_thenItReturns200OK() throws IOException {

        logger.info("Configured retires: {}", retriesFor200OKTest);
        logger.info("configure timeout for REST Assured: {}", timeOutFor200OKTest);
        logger.info("Input files:{}", pageStausCheckUrlFileNames);
        logger.info("Mode: {}", modeFor200OKTest);

        Multimap<String, Integer> badURLs = ArrayListMultimap.create();
        RestAssuredConfig restAssuredConfig = TestUtils.getRestAssuredCustomConfig(timeOutFor200OKTest);
        Retryer<Boolean> retryer = Utils.getGuavaRetryer(retriesFor200OKTest);

        try (Stream<String> alURls = Utils.fetchFilesAsList(pageStausCheckUrlFileNames)) {
            alURls.forEach(URL -> {
                //TestUtils.sleep(400);
                String fullURL = page.getBaseURL() + URL;
                logger.info("Verifying 200OK on: {}", fullURL);

                TestUtils.hitURLUsingGuavaRetryer(restAssuredConfig, fullURL, badURLs, retryer, modeFor200OKTest);

            });
        }

        if (badURLs.size() > 0) {
            recordMetrics(badURLs.keySet().size(), FAILED);
            fail("200OK Not received from following URLs:\n" + Utils.http200OKTestResultBuilder(badURLs));
        }
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheArticleWithSeries_whenArticleLoads_thenPluginLoadsProperly() {
        page.setUrl(page.getBaseURL() + GlobalConstants.ARTICLE_WITH_SERIES);

        page.loadUrl();

        assertTrue(page.seriesPluginElementDisplayed());

    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheArticleWithPersistenceEBookDownload_whenPageLoads_thenFooterImageIsDisplayed() {
        page.setUrl(page.getBaseURL() + GlobalConstants.ARTICLE_WITH_PESISTENCE_EBOOK_DOWNLOAD);

        page.loadUrl();

        page.getPathOfPersistenceEBookImages().forEach(image -> {
            assertEquals(200, RestAssured.given().head(image.getAttribute("src")).getStatusCode());
        });

    }

    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#gaCodeTestDataProvider")
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenAGoogleAnalyticsEnabledPage_whenAnalysingThePageSource_thenItHasTrackingCode(String url) {
        String  fullUrl = page.getBaseURL() + url;
        
        page.setUrl(fullUrl);

        page.loadUrl();

        assertTrue(page.getAnalyticsScriptCount() == 1, "GA script count is not equal to 1 on " + fullUrl);
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenBaeldungFeedUrl_whenUrlIsHit_thenItRedirectsToFeedburner() {
        Response response = RestAssured.given().redirects().follow(false).get(GlobalConstants.BAELDUNG_FEED_URL);

        assertTrue(response.getStatusCode() == 301 || response.getStatusCode() == 302, "HTTP staus code is not 301 or 302. Returned status code is: " + response.getStatusCode());
        assertTrue(response.getHeader("Location").replaceAll("/$", "").trim().toLowerCase().contains(GlobalConstants.BAELDUNG_FEED_FEEDFLITZ_URL),
                "Location header doesn't contain eeds.feedblitz.com/baeldung. Returned Location header is: " + response.getHeader("Location").replaceAll("/$", "").toLowerCase());
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheCategoryPage_whenThePageLoads_thenItContainsNoindexRobotsMeta() {
        page.setUrl(page.getBaseURL() + GlobalConstants.CATEGORY_URL);

        page.loadUrl();

        assertTrue(page.metaWithRobotsNoindexEists(), "Couldn't find noindex robot meta tag on /category/series/");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheTagPage_whenThePageLoads_thenItContainsNoindexRobotsMeta() {
        page.setUrl(page.getBaseURL() + GlobalConstants.TAG_ARTICLE_URL);

        page.loadUrl();

        assertTrue(page.metaWithRobotsNoindexEists(),"Couldn't find noindex robot meta tag on /tag/activiti/");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    @Tag(GlobalConstants.GA_TRACKING)
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public final void givenOnTheCoursePage_whenPageLoads_thenTrackingIsSetupCorrectly() throws JsonProcessingException, IOException {

        Multimap<String, String> badURLs = ArrayListMultimap.create();
        Multimap<String, List<EventTrackingVO>> testData = Utils.getCoursePagesGATrackingTestData(objectMapper);
        for (String urlKey : testData.keySet()) {
            page.setUrl(page.getBaseURL() + urlKey);

            page.loadUrl();
            for (List<EventTrackingVO> eventTrackingVOs : testData.get(urlKey)) {
                for (EventTrackingVO eventTrackingVO : eventTrackingVOs) {
                    logger.debug("Asserting: " + eventTrackingVO.getTrackingCodes() + " for on " + page.getBaseURL() + urlKey);
                    if (!page.findDivWithEventCalls(eventTrackingVO.getTrackingCodes())) {
                        badURLs.put(page.getBaseURL() + urlKey, "Button/link: " + eventTrackingVO.getLinkText() + "\nTracking code: " + eventTrackingVO.getTrackingCodes());
                    }
                }
                if (!page.findEventGenerationScript()) {
                    badURLs.put(page.getBaseURL() + urlKey, "event generation script not found");
                }
            }

        }

        if (badURLs.size() > 0) {
            recordMetrics(badURLs.size(), FAILED);
            triggerTestFailure(Utils.gaTrackingSetopResultBuilder(badURLs), "Couldn't find the tracking code  on the below pages");
        }
    }

    @Test
    @Tag("screenShotTest")
    public final void screenShotTest() throws IOException {
        page.setUrl(page.getBaseURL() + "/jackson");
        
        TestUtils.sleep(30000);

        page.loadUrl();

        TestUtils.takeScreenShot(page.getWebDriver());
       
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenAnArticleWithTheDripScript_whenTheArticleLoads_thenTheArticleHasTheDripScrip() {
        page.setUrl(page.getBaseURL() + GlobalConstants.ARTICLE_WITH_DRIP_SCRIPT);

        page.loadUrl();

        assertTrue(page.getDripScriptCount() == 1, "Drip script count is not equal to 1");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenAPageWithTheDripScript_whenThePageLoads_thenThePageHasTheDripScrip() {
        page.setUrl(page.getBaseURL() + GlobalConstants.PAGE_WITH_DRIP_SCRPT);

        page.loadUrl();

        assertTrue(page.getDripScriptCount() == 1, "Drip script count is not equal to 1");
    }

    @Test
    @Tag(GlobalConstants.TAG_GITHUB_RELATED)
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public final void givenAGitHubModuleReadme_whenAnalysingTheReadme_thenLinksToAndFromGithubMatch(TestInfo testInfo) throws IOException {
        
        List<String> testExceptions= YAMLProperties.exceptionsForTests.get(TestUtils.getMehodName(testInfo.getTestMethod()));

        tutorialsRepoCrawlerController.startCrawlingWithAFreshController(CrawlerForFindingReadmeURLs.class, Runtime.getRuntime().availableProcessors());

        List<String> readmeURLs = Utils.getDiscoveredLinks(tutorialsRepoCrawlerController.getDiscoveredURLs());
        
        Multimap<String, LinkVO> badURLs = ArrayListMultimap.create();
        
        Map<String, Integer> articleCountByReadme = new HashMap<>();

        readmeURLs.forEach(readmeURL -> {
            try {
                
                page.setUrl(readmeURL);

                page.loadUrl(); // loads README in browser

                List<LinkVO> urlsInReadmeFile = page.getLinksToTheBaeldungSite(); // get all the articles linked in this README
                
                // for documenting no of links per README
                if (readmeURL.toLowerCase().contains("spring")) {
                    if (urlsInReadmeFile.size() > limitForSpringRelatedReadmeHavingArticles) {
                        articleCountByReadme.put(readmeURL, urlsInReadmeFile.size());
                    }

                } else if (urlsInReadmeFile.size() > limitForReadmeHavingArticles) {
                    articleCountByReadme.put(readmeURL, urlsInReadmeFile.size());
                }
                
                if(testExceptions.contains(readmeURL)) {
                    return;
                }
                
                String reamdmeParentURL = Utils.getTheParentOfReadme(readmeURL);
                urlsInReadmeFile.forEach(link -> {                    
                    String staging8Url = Utils.changeLiveUrlWithStaging8(link.getLink());
                    
                    page.setUrl(staging8Url);
                    page.loadUrl(); // loads an article in the browser
                    if (!page.getWebDriver().getPageSource().toLowerCase().contains(reamdmeParentURL.toLowerCase())) {
                        badURLs.put(readmeURL, link);
                    }
                });
            } catch (Exception e) {
                logger.debug("Error while processing " + readmeURL + " \nError message" + e.getMessage());
            }
        });

        if (badURLs.size() > 0 || articleCountByReadme.size() > 0) {
            recordMetrics(badURLs.size(), TestMetricTypes.FAILED);
            recordMetrics(articleCountByReadme.size(), TestMetricTypes.FAILED);           
            failTestWithLoggingTotalNoOfFailures("\nwe found issues with following READMEs" + Utils.getErrorMessageForInvalidLinksInReadmeFiles(badURLs)
                    + Utils.compileReadmeCountResults(articleCountByReadme, GlobalConstants.GivenAGitHubModuleReadme_whenAnalysingTheReadme_thentheReadmeDoesNotLikTooManyArticles));

        }
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnTheCoursePage_whenThePageLoads_thenAGeoIPApiProviderWorks() {
        page.setUrl(page.getBaseURL() + GlobalConstants.COURSE_PAGE_FOR_VAT_TEST);

        page.loadUrl();

        assertTrue(page.geoIPProviderAPILoaded(), "geoIP API provider is not working. VAT Calc Notice message not populated in the geovat-info div");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnTheBaeldungRSSFeed_whenTheFirstUrlIsHit_thenItPointsToTheBaeldungSite() {
        page.setUrl(GlobalConstants.BAELDUNG_RSS_FEED_URL);

        page.loadUrl();

        page.setUrl(page.getTheFirstBaeldungURL());
        page.loadUrl();

        logger.info("Currently loaded page URL: " + page.getWebDriver().getCurrentUrl());
        logger.info("Currently loaded page title: " + page.getWebDriver().getTitle());
        logger.info("Currently set feed url: " + page.getUrl());

        assertTrue(page.rssFeedURLPointsTotheBaeldungSite(page.getWebDriver().getCurrentUrl()), "The RSS Feed URL doesn't point to  https://baeldung.com");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheBaeldungRSSFeed_whenAnalysingFeed_thenItIsUptoDate() {
        page.setUrl(GlobalConstants.BAELDUNG_RSS_FEED_URL);

        page.loadUrl();

        MatcherAssert.assertThat("Baeldung RSS Feed should not be older than " + rssFeedShouldNotbeOlderThanDays + "days", page.getAgeOfTheFirstPostIntheFeed(), Matchers.lessThanOrEqualTo(rssFeedShouldNotbeOlderThanDays));
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnCoursePages_whenAPageLoads_thenItContainsImportantAnchors() {

        page.setUrl(page.getBaseURL() + GlobalConstants.COURSE_RWS_PAGE);
        logger.info("RWS course page loaded");

        page.loadUrl();

        assertTrue(page.tableAnchorIsVisibleOnThePage(), "RWS Course page is missing #table(PRICING) anchor in the footer");
        assertTrue(page.masterclassAnchorIsVisibleOnThePage(), "RWS Course page is missing #master-class anchor in the footer");
        assertTrue(page.certificationclassAnchorIsVisibleOnThePage(), "RWS Course page is missing #certification-class anchor in the footer");

        page.setUrl(page.getBaseURL() + GlobalConstants.COURSE_LSS_PAGE);
        logger.info("LSS course page loaded");

        page.loadUrl();

        assertTrue(page.tableAnchorIsVisibleOnThePage(), "RWS Course page is missing #table(PRICING) anchor in the footer");
        assertTrue(page.masterclassAnchorIsVisibleOnThePage(), "RWS Course page is missing #master-class anchor in the footer");
        assertTrue(page.certificationclassAnchorIsVisibleOnThePage(), "RWS Course page is missing #certification-class anchor in the footer");
    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheBaeldungMediaKitURL_whenPageLoads_thenItReturns200OK() throws IOException {

        URL url = new URL(GlobalConstants.BAELDUNG_MEDIA_KIT_URL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");

        assertEquals(200, httpURLConnection.getResponseCode());

    }

    @Test
    @Tag(GlobalConstants.TAG_DAILY)
    @Tag(GlobalConstants.TAG_SKIP_METRICS)
    public final void givenURLsWithAnchorsLinkingWithinSamePage_whenAnaysingPage_thenAnHtmlElementExistsForEachAnchor() throws JsonProcessingException, IOException {

        List<AnchorLinksTestDataVO> AnchorLinksTestDataVOs = Utils.getAnchorLinksTestData(objectMapper);
        Multimap<String, String> badURLs = ArrayListMultimap.create();

        for (AnchorLinksTestDataVO anchorLinksTestData : AnchorLinksTestDataVOs) {
            page.setUrl(page.getBaseURL() + anchorLinksTestData.getUrl());

            page.loadUrl();

            for (String anchorLink : anchorLinksTestData.getAnchorsLinks()) {
                if (!page.findElementForAnchor(anchorLink)) {
                    badURLs.put(page.getUrl(), "\n" + anchorLink);
                }
            }
        }

        if (badURLs.size() > 0) {
            recordMetrics(badURLs.keySet().size(), FAILED);
            triggerTestFailure(badURLs, "Matching HTML element not found for the following Anchor Links");
        }
    }

    @ParameterizedTest(name = " {displayName} - Test {0} redirects to {1}")
    @MethodSource("com.baeldung.utility.TestUtils#redirectsTestDataProvider")
    @Tag("redirectsTest")
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenTheListOfRedirectedUrls_whenAUrlLoads_thenItRedirectsSuccesfully(String url, String redirectedTo) {
        String fullUrl = url;
        if (!url.contains("http://")) {
            fullUrl = page.getBaseURL() + url;
        }
        Response response = RestAssured.given().redirects().follow(false).get(fullUrl);

        assertTrue(Utils.addTrailingSlasIfNotExists(response.getHeader("Location").toLowerCase()).equals(Utils.addTrailingSlasIfNotExists(redirectedTo.toLowerCase())), url + " doesn't redirec to " + redirectedTo);
    }

    @Test
    public final void givenTheContactForm_whenAMessageIsSubmitted_thenItIsSentSuccessfully() throws InterruptedException {

        // load contact form
        String fullUrl = page.getBaseURL() + GlobalConstants.CONTACT_US_FORM_URL;
        logger.info(modeFor200OKTest);
        page.setUrl(fullUrl);
        page.loadUrl();

        page.acceptCookie();

        // fill and submit form       
        page.getWebDriver().findElement(By.xpath("//input[@name='form_fields[name]' or @name='your-name']")).sendKeys("Selenium Test on " + LocalDate.now());
        page.getWebDriver().findElement(By.xpath("//input[@name='form_fields[email]' or @name='your-email']")).sendKeys("support@baeldung.com");
        
        page.getWebDriver().findElement(By.xpath("//textarea[@name='form_fields[message]' or @name='your-message']")).sendKeys("Test message from Selenium");        
        // page.acceptCookie();
        page.getWebDriver().findElement(By.xpath("//input[contains(@value, 'Send your message')] | //button//*[contains(text(), 'Send')]")).click();

        // verify
        WebDriverWait wait = new WebDriverWait(page.getWebDriver(), 30);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Thank you for your message. It has been sent') or contains(text(), 'The form was sent successfully.')]")));
        } catch (Exception e) {
            recordMetrics(1, FAILED);
            fail("Contact form not working. Contact Form url: " + fullUrl);
        }
        logger.info("message sent successfully from {}", fullUrl);

    }

    @ParameterizedTest(name = " {displayName} - verify footer links on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesAnchorLinksTestDataProvider()")
    @Tag(GlobalConstants.TAG_SITE_SMOKE_TEST)
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenURLsWithFooterLinks_whenAnaysingFooterLinks_thenAnchorTextAndAnchorLinksExist(String url, String footerTag, List<FooterLinksDataVO.Link> footerLinks) throws JsonProcessingException, IOException {
        page.setUrl(page.getBaseURL() + url);

        page.loadUrl();

        logger.info("Inspection footer links on {}", page.getBaseURL() + url);
        List<Executable> tests = new ArrayList<>();
        for (FooterLinksDataVO.Link link : footerLinks) {
            if (shouldSkipLink(link.getLinkCategory())) {
                logger.info("Skipping {}",link.getAnchorLink());
                continue;
            }
            tests.add(()-> assertTrue(page.anchorAndAnchorLinkAvailable(footerTag, link), String.format("Countn't find Anchor Text:%s and Anchor Link: %s, on %s", link.getAnchorText(), link.getAnchorLink(), url)));            
        }

        assertAll(tests.stream());
    }

    private boolean shouldSkipLink(FooterLinkCategory linkCategory) {
        return Optional.ofNullable(linkCategory)
                .filter(category -> category.equals(FooterLinkCategory.WRITE_FOR_BAELDUNG))
                .map(category -> !verifyWriteForBaeldungFooterLink)
                .orElse(false);
    }

    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#consoleLogTestDataProvider")
    @Tag(GlobalConstants.TAG_DAILY)
    @Tag(GlobalConstants.TAG_SITE_SMOKE_TEST)
    public final void givenAPage_whenThePageLoads_thenNoSevereMessagesInTheBrowserConsoleLog(String url) {
        page.setUrl(page.getBaseURL() + url);

        page.loadUrl();

        LogEntries browserLogentries = page.getWebDriver().manage().logs().get(LogType.BROWSER);

        for (LogEntry logEntry : browserLogentries) {
            if (logEntry.getLevel().equals(Level.SEVERE)) {
                fail("Error with Severe Level-->" + logEntry.getMessage());
            }
        }
    }

    @ParameterizedTest(name = " {displayName} - verify purchase links on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#pagesPurchaseLinksTestDataProvider()")    
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenOnTheCoursePage_whenAnaysingThePage_thenThePurchaseLinksAreSetupCorrectly(String courseUrl, List<PurchaseLink> purchaseLinks) {

        String fullURL = page.getBaseURL() + courseUrl;

        page.setUrl(fullURL);
        page.loadUrl();       
        
        List<Executable> testLikIds = new ArrayList<>();
        List<Executable> testLikRedirects = new ArrayList<>();
        
        for (PurchaseLink link : purchaseLinks) {
            logger.info(magentaColordMessage("veryfing that link:{} exists and redirects to {} "), link.getLink(), link.getRedirectsTo());
            testLikIds.add(() -> assertTrue(page.linkIdAndLinkAvailable(link), String.format("Countn't find Purchse link with id:%s and Link: %s, on %s", link.getLinkId(), link.getLink(), fullURL)));            
            testLikRedirects.add(() -> assertTrue(TestUtils.veirfyRedirect(restAssuredConfig, link.getLink(), link.getRedirectsTo(), page), link.getLinkId() + " (" + link.getLink() + ") on " + fullURL + " doesn't redirec to " + link.getRedirectsTo()));
            
        }
        assertAll(testLikIds.stream());
        assertAll(testLikRedirects.stream());
    }
    
    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#popupTestDataProvider")
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenAPage_whenThePageLoads_thenNoPopupAppearsOnThePage(String url, TestInfo testInfo) {      
        
        String fullUrl = page.getBaseURL() + url;
        logger.info("Processing " + fullUrl);
        logger.info("Sleep time configured as:" + timeToWaitForPopup);
        
        
        page.setUrl(fullUrl);

        page.loadUrl();
        
        TestUtils.sleep(timeToWaitForPopup);

        page.findElentWithHref("privacy-policy")
                .ifPresent(element -> element.click());
    }

    @Test    
    public final void givenAnArtifactId_thenListAllChildModules() throws IOException, GitAPIException {

        if (StringUtils.isBlank(parentArtifactId)) {
            logger.info(magentaColordMessage("Parent Artifact ID not provided. The build will be aborted now. "));
            fail("Parent Artificat ID is required");
        }

        //fetch tutorials repo
        String repoLocalDirectory = GlobalConstants.tutorialsRepoLocalPath;
        Path repoDirectoryPath = Paths.get(repoLocalDirectory);
        Utils.fetchGitRepo(this.redownloadTutorialsRepo, repoDirectoryPath, GlobalConstants.tutorialsRepoGitUrl);

        TutorialsParentModuleFinderFileVisitor tutorialsParentModuleFinderFileVisitor = new TutorialsParentModuleFinderFileVisitor(parentArtifactId);
        Files.walkFileTree(repoDirectoryPath, tutorialsParentModuleFinderFileVisitor);
        Utils.logChildModulesResults(tutorialsParentModuleFinderFileVisitor);

        logger.info(ConsoleColors.magentaColordMessage("finished"));
    }
    

    @Test    
    public final void givenTutorialsRepo_whenAllModulesAnalysed_thenFolderNameAndArtifiactIdAndModuleNameMatch() throws IOException, GitAPIException {
        //fetch tutorials repo
        String repoLocalDirectory = GlobalConstants.tutorialsRepoLocalPath;
        Path repoDirectoryPath = Paths.get(repoLocalDirectory);
        Utils.fetchGitRepo(this.redownloadTutorialsRepo, repoDirectoryPath, GlobalConstants.tutorialsRepoGitUrl);               
        
        ModuleAlignmentValidatorFileVisitor moduleAlignmentValidatorFileVisitor = new ModuleAlignmentValidatorFileVisitor();
        Files.walkFileTree(repoDirectoryPath, moduleAlignmentValidatorFileVisitor);
        
        Utils.logUnAlignedModulesResults(moduleAlignmentValidatorFileVisitor);
        Utils.logUnparsableModulesResults(moduleAlignmentValidatorFileVisitor);
        if (moduleAlignmentValidatorFileVisitor.getInvalidModules().size() > 0) {
            recordMetrics(moduleAlignmentValidatorFileVisitor.getInvalidModules().size(), FAILED);
            fail("Unaligned modules found. Please refer to the console log for details");
        }                
        
        logger.info(ConsoleColors.magentaColordMessage("finished"));
    }    
    
}
