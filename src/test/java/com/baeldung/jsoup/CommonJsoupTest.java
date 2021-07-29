package com.baeldung.jsoup;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.baeldung.common.BaseTest;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.TestMetricsExtension;
import com.baeldung.utility.TestUtils;

@ExtendWith(TestMetricsExtension.class)
public class CommonJsoupTest extends BaseTest{
    
    @ParameterizedTest(name = " {displayName} - on {0}")
    @MethodSource("com.baeldung.utility.TestUtils#thankYouPagesUrlsProvider")
    @Tag(GlobalConstants.TAG_DAILY)
    public final void givenAThankYouPage_whenThePageLoads_thenTheItHasTheFacebookCode(String url) throws IOException {      
                
        logger.info("Processing " + url);
        Document doc = Jsoup.connect(url).get();       
        assertAll(() -> assertTrue(TestUtils.faceBookMainScripttExists.apply(doc, url), String.format("Facebook main tracking code not found on %s", url)),
                  () -> assertTrue(TestUtils.faceBookEventsScripttExists.apply(doc, url), String.format("Facebook event tracking code not found on %s", url)));
       
    }

}
