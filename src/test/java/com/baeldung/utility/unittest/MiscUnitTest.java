package com.baeldung.utility.unittest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.baeldung.common.Utils;

public class MiscUnitTest {
    
    @Test
    void givenLiveUrlsWith_whenChangedToStaging8_thenItIsValidStaging8Url() {
        String liveUrlWithHttps = "https://www.baeldung.com/spring-security-vs-apache-shiro";
        String expectedStaging8url1 = "http://staging8.baeldung.com/spring-security-vs-apache-shiro";
        
        String liveUrlNonHttps = "http://www.baeldung.com/apache-shiro";
        String expectedStaging8url2 = "http://staging8.baeldung.com/apache-shiro";
        
        String liveUrlWithoutPrototol = "www.baeldung.com/apache-shiro-access-control";
        String expectedStaging8url3 = "http://staging8.baeldung.com/apache-shiro-access-control";
        
        Assertions.assertAll("When live url is converted to staging8 then it should be a valid staging8 url",
                () -> Assertions.assertEquals(expectedStaging8url1, Utils.changeLiveUrlWithStaging8(liveUrlWithHttps)),
                () -> Assertions.assertEquals(expectedStaging8url2, Utils.changeLiveUrlWithStaging8(liveUrlNonHttps)),
                () -> Assertions.assertEquals(expectedStaging8url3, Utils.changeLiveUrlWithStaging8(liveUrlWithoutPrototol)));
    }
    
}
