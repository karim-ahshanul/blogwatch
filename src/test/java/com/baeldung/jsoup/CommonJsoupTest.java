package com.baeldung.jsoup;

import static com.baeldung.common.ConsoleColors.greenBoldMessage;
import static com.baeldung.common.ConsoleColors.magentaColordMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;

import com.baeldung.utility.TestUtils;

@ExtendWith(TestMetricsExtension.class)
public class CommonJsoupTest extends BaseJsoupTest{
    
    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#thankYouPagesUrlsProvider")
    @Tag(GlobalConstants.THANKYOU_PAGES_EVENT_TRACKING)
    public final void givenAThankYouPage_whenThePageLoads_thenTheItHasTheFacebookEventTrackingCode(String url) throws IOException {      
                
        logger.info("Processing " + url);
        Document doc = Jsoup.connect(url).get();       
        assertAll(() -> assertTrue(TestUtils.facebookMainEventTrackingScriptExists.apply(doc, url), String.format("Facebook main tracking code not found on %s", url)),
                  () -> assertTrue(TestUtils.facebookEventPriceTrackingScriptExists.apply(doc, url), String.format("Facebook event price tracking code not found on %s", url)));
       
    }
    
    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#thankYouPagesUrlsProvider")
    @Tag(GlobalConstants.THANKYOU_PAGES_EVENT_TRACKING)
    public final void givenAThankYouPage_whenThePageLoads_thenTheItHasTheDripEventTrackingCode(String url) throws IOException {      
                
        logger.info("Processing " + url);
        Document doc = Jsoup.connect(url).get();       
        assertAll(() -> assertTrue(TestUtils.dripMainEventTrackingScriptExists.apply(doc, url), String.format("Drip main tracking code not found on %s", url)),
                  () -> assertTrue(TestUtils.dripEventPriceTrackingScriptExists.apply(doc, url), String.format("Drip[ event price tracking code not found on %s", url)));
       
    }
    
    @ParameterizedTest(name = "verify ad-slots  on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#adsSlotsTestDataProvider")
    public final void givenAPageWithAds_whenThePageLoads_thenAdSlotsAreAvailableOnThePage(String url, List<String> slotIds) throws IOException {
        String fullUrl = baseURL + url;
                 
        logger.info(greenBoldMessage("inspecting:{}  "), fullUrl);
        Document doc = Jsoup.connect(fullUrl).get(); 
        List<Executable> tests = new ArrayList<>();
        
        for(String slotId: slotIds) {
            logger.info(magentaColordMessage("looking for ad-slot:{} on {} "), slotId, fullUrl);
            tests.add(() -> assertTrue(TestUtils.scriptWithAdSlotExists.apply(doc, slotId), String.format("Countn't find tagId:%s on %s", slotId, fullUrl)));
        }        
        assertAll(tests.stream());

    }

}
