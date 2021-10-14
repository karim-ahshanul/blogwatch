package com.baeldung.selenium.unittest;

import static com.baeldung.common.Utils.replaceTutorialLocalPathWithHttpUrl;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

public class CommonUnitTest {

    @Test
    void givenAReadmeWithLocalSystemPath_whenConvertToHttpURL_itReturn200OK(){
        
        String localSystemPath="/var/lib/jenkins/tutorials-source-code/akka-streams/README.md";
        String httpUrl = replaceTutorialLocalPathWithHttpUrl.apply(localSystemPath);
        
        assertTrue(RestAssured.given().get(httpUrl).getStatusCode() == 200);
        
    }
}
