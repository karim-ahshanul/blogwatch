package com.baeldung.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;

import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.ConsoleColors;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.baeldung.common.YAMLProperties;
import com.baeldung.common.vo.AdSlotsVO;
import com.baeldung.common.vo.CoursePurchaseLinksVO;
import com.baeldung.common.vo.FooterLinksDataVO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.baeldung.site.SitePage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.google.common.collect.Multimap;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;

public class TestUtils {
    protected static Logger logger = LoggerFactory.getLogger(TestUtils.class);

    protected static RestAssuredConfig restAssuredConfig = TestUtils.getRestAssuredCustomConfig(3000);

    private static ObjectMapper ObjectMapper = new ObjectMapper();

    public static boolean articleLinkFoundOnTheGitHubModule(List<String> gitHubModuleLinks, String articleRelativeURL, SitePage page) {
        boolean articleLinkFoundOnGitHubModule = false;

        for (String url : gitHubModuleLinks) {
            page.getWebDriver().get(url); // load GitHub module URL
            if (page.linkExistsInthePage(articleRelativeURL)) {
                articleLinkFoundOnGitHubModule = true;
                break;
            }
        }
        return articleLinkFoundOnGitHubModule;
    }

    public static RestAssuredConfig getRestAssuredCustomConfig(int timeout) {
        // @formatter:off
        return RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeout)
                        .setParam("http.socket.timeout", timeout)
                        .setParam("http.connection-manager.timeout",timeout));
     // @formatter:on
    }

    public static Boolean inspectURLHttpStatusCode(RestAssuredConfig restAssuredConfig, String fullURL, Multimap<String, Integer> badURLs, String modeFor200OKTest) {
        try {
            int httpStatusCode = RestAssured.given().config(restAssuredConfig).get(fullURL).getStatusCode();

            if (HttpStatus.SC_OK == httpStatusCode) {
                if (!badURLs.get(fullURL).isEmpty()) {
                    if (GlobalConstants.MODE_RELAXED.equalsIgnoreCase(modeFor200OKTest)) {
                        badURLs.removeAll(fullURL);
                    } else {
                        badURLs.put(fullURL, httpStatusCode);
                    }
                }
                return true;
            } else if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
                logger.info("{} return by {}", httpStatusCode, fullURL);
                badURLs.put(fullURL, httpStatusCode);
                return true;
            } else {
                logger.info(httpStatusCode + " Status code received from: " + fullURL);
                badURLs.put(fullURL, httpStatusCode);
                return null;
            }

        } catch (Exception e) {
            logger.error("Got error while retrieving HTTP status code for:" + fullURL);
            logger.error("Error Message: " + e.getMessage());
            badURLs.put(fullURL, -1);
            /*
             * if (logger.isDebugEnabled()) { e.printStackTrace(); }
             */
            return null;
        }
    }

    public static Boolean inspectURLHttpStatusCode(RestAssuredConfig restAssuredConfig, String fullURL) {
        try {
            int httpStatusCode = RestAssured.given().header("cache-control", "no-cache").config(restAssuredConfig).head(fullURL).getStatusCode();

            if (HttpStatus.SC_OK == httpStatusCode) {
                return true;
            }
            logger.error(httpStatusCode + " received from: {} ", fullURL);
            return false;
        } catch (Exception e) {
            logger.error("Got error while retrieving HTTP status code for:" + fullURL);
            logger.error("Error Message: " + e.getMessage());
            return true;
        }
    }

    public static int getHttpStatusCode(String URL) {
        try {
            URL pageURL = new URL(URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) pageURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
            logger.error("Got error while retrieving HTTP status code for:" + URL);
            logger.error("Error Message: " + e.getMessage());
            return -1;
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //
        }

    }

    public static void hitURLUsingGuavaRetryer(RestAssuredConfig restAssuredConfig, String fullURL, Multimap<String, Integer> badURLs, Retryer<Boolean> retryer, String modeFor200OKTest) {
        try {
            retryer.call(() -> TestUtils.inspectURLHttpStatusCode(restAssuredConfig, fullURL, badURLs, modeFor200OKTest));
        } catch (RetryException e) {
            logger.error("Finished {} retries for {}", e.getNumberOfFailedAttempts(), fullURL);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public static Map<Integer, String> getHTTPStatusCodesOtherThan200OK(List<String> gitHubModulesLinkedOntheArticle) {

        int httpStatusCode = HttpStatus.SC_OK;
        Map<Integer, String> httpStatusCodesOtherThan200OK = new HashMap<>();
        for (String url : gitHubModulesLinkedOntheArticle) {
            try {
                httpStatusCode = RestAssured.given().config(restAssuredConfig).head(url).getStatusCode();

                if (HttpStatus.SC_OK != httpStatusCode) {
                    logger.error(httpStatusCode + " received from: {} ", url);
                    httpStatusCodesOtherThan200OK.put(httpStatusCode, url);
                }
            } catch (Exception e) {
                httpStatusCode = HttpStatus.SC_OK;
                logger.error("Got error while retrieving HTTP status code for:" + url);
                logger.error("Error Message: " + e.getMessage());
            }
        }
        return httpStatusCodesOtherThan200OK;
    }

    public static Stream<Arguments> redirectsTestDataProvider() {
        return YAMLProperties.redirectsTestData.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));

    }

    public static void takeScreenShot(WebDriver webdriver) {
        try {
            FileUtils.copyFile(((TakesScreenshot) webdriver).getScreenshotAs(OutputType.FILE), new File(String.format("/tmp/screenshots/screenshot%s.png", LocalDateTime.now())));
        } catch (Exception e) {
            logger.error("Error while taking screenshot: {}", e.getMessage());
        }
    }

    public static Stream<Arguments> pagesAnchorLinksTestDataProvider() throws JsonParseException, JsonMappingException, IOException {

        List<FooterLinksDataVO> footerLinksDataVOs = ObjectMapper.readValue(Utils.getJsonResourceFile("./page-anchor-links-test-data.json"), new TypeReference<List<FooterLinksDataVO>>() {
        });

        return footerLinksDataVOs.stream().flatMap(testSet -> testSet.getUrls().stream().map(entry -> Arguments.of(entry, testSet.getFooterTag(), testSet.getFooterLinks())));

    }

    public static Stream<Arguments> gaCodeTestDataProvider() {
        return YAMLProperties.multiSiteTargetUrls.get(GlobalConstants.givenAGoogleAnalyticsEnabledPage_whenAnalysingThePageSource_thenItHasTrackingCode).stream().map(entry -> Arguments.of(entry));
    }

    public static Stream<Arguments> consoleLogTestDataProvider() {
        return YAMLProperties.multiSiteTargetUrls.get(GlobalConstants.givenAPage_whenThePageLoads_thenNoSevereMessagesInTheBrowserConsoleLog).stream().map(entry -> Arguments.of(entry));
    }

    public static Stream<Arguments> pagesPurchaseLinksTestDataProvider() throws JsonParseException, JsonMappingException, IOException {

        List<CoursePurchaseLinksVO> coursePurchaseLinksVOs = ObjectMapper.readValue(Utils.getJsonResourceFile("./course-pages-purchase-links-test-data.json"), new TypeReference<List<CoursePurchaseLinksVO>>() {
        });

        return coursePurchaseLinksVOs.stream().map(entry -> Arguments.of(entry.getCourseUrl(), entry.getPurchaseLinks()));

    }

    public static boolean veirfyRedirect(RestAssuredConfig restAssuredConfig, String link, String exprectRedirectTo) {
        try {
            return RestAssured.given().config(restAssuredConfig).get(link).getHeader("Location").toLowerCase().contains(exprectRedirectTo);
        } catch (Exception e) {
            logger.error("Error while verifying redirect. Error Message: {}", e.getMessage());
            return false;
        }       
    }
    
    public static boolean veirfyRedirect(RestAssuredConfig restAssuredConfig, String link, String exprectRedirectTo, SitePage page) {
        try {
            page.setUrl(link);
            page.loadUrl();
            return page.getWebDriver().getCurrentUrl().toLowerCase().contains(exprectRedirectTo);
        } catch (Exception e) {
            logger.error("Error while verifying redirect. Error Message: {}", e.getMessage());
            return false;
        }       
    }

    public static Stream<Arguments> gaCodeTestDataProviderForDraftSite() {
        return YAMLProperties.multiSiteTargetUrls.get(GlobalConstants.givenAGoogleAnalyticsEnabledPageOnTheDraftSite_whenAnalysingThePageSource_thenItHasTrackingCode).stream().map(entry -> Arguments.of(entry));
    }
    
    public static Stream<Arguments> popupTestDataProvider() {
        return YAMLProperties.multiSiteTargetUrls.get(GlobalConstants.givenAPage_whenThePageLoads_thenNoPopupAppearsOnThePage).stream().map(entry -> Arguments.of(entry));
    }

    public static String getMehodName(Optional<Method> testMethod) {
        return testMethod.map(method -> method.getName()).orElse(null);
        
    }  
    
    public static Stream<Arguments> adsSlotsTestDataProvider() throws JsonParseException, JsonMappingException, IOException {

        List<AdSlotsVO> adSlotsVO = ObjectMapper.readValue(Utils.getJsonResourceFile("./ads-test-data.json"), new TypeReference<List<AdSlotsVO>>() {
        });

        return adSlotsVO.stream().map(entry -> Arguments.of(entry.getUrl(), entry.getSlotIds()));

    }
    
    public static Stream<Arguments> thankYouPagesUrlsProvider() throws IOException {
       return Utils.fetchFileAsStream("baeldung-thankyou-pages.txt").map(url -> Arguments.of(url));        
    }
    
    public static BiFunction< Document, String,Boolean> facebookMainEventTrackingScriptExists = (doc, url) -> {
        try {               
            return doc.select("script:containsData("+ GlobalConstants.FACEBOOK_MAIN_EVENT_TRACKING_SCRIPT+")").size() > 0;           
        } catch (Exception e) {
            logger.error(ConsoleColors.redBoldMessage("Error which connecting to {}, error message: {}  "), url, e.getMessage());
            return false;
        }
    };
    
    public static BiFunction<Document, String,Boolean> facebookEventPriceTrackingScriptExists = (doc, url) -> {
        try {            
            return  doc.select("script:containsData(fbq)").stream().map(Element::toString).filter(t -> t.contains(GlobalConstants.FACEBOOK_EVENT_PRICE_TRACKING_SCRIPT)).findFirst().isPresent();                      
        } catch (Exception e) {            
            logger.error(ConsoleColors.redBoldMessage("Error which connecting to {}, error message: {}  "), url, e.getMessage());
            return false;
        }
    };
    
    public static BiFunction< Document, String,Boolean> dripMainEventTrackingScriptExists = (doc, url) -> {
        try {               
            return doc.select("script:containsData("+ GlobalConstants.DRIP_MAIN_EVENT_TRACKING_SCRIPT+")").size() > 0;           
        } catch (Exception e) {
            logger.error(ConsoleColors.redBoldMessage("Error parsing the document for {}, error message: {}  "), url, e.getMessage());
            return false;
        }
    };
    
    public static BiFunction<Document, String,Boolean> dripEventPriceTrackingScriptExists = (doc, url) -> {
        try {            
            return  doc.select("script:containsData(window._dcq)").stream().map(Element::toString).filter(t -> t.contains(GlobalConstants.DRIP_EVENT_PRICE_TRACKING_SCRIPT)).findFirst().isPresent();                      
        } catch (Exception e) {            
            logger.error(ConsoleColors.redBoldMessage("Error parsing the document for {}, error message: {}  "), url, e.getMessage());
            return false;
        }
    };
    
    public static BiFunction< Document, String, Boolean> scriptWithAdSlotExists = (doc, slotId) -> {
        try {               
            return doc.select("script:containsData("+ slotId +")").size() > 0;           
        } catch (Exception e) {
            logger.error(ConsoleColors.redBoldMessage("Error parsing the document, error message: {}  "), e.getMessage());
            return false;
        }
    };
}
