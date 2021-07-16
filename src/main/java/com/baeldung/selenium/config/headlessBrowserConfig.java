package com.baeldung.selenium.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;

public class headlessBrowserConfig extends browserConfig {

    @Value("${headless.browser.name}")
    private String headlessBrowserName;

    public String getHeadlessBrowserName() {
        return headlessBrowserName;
    }

    @Override
    public void openNewWindow() {
        logger.info("headlessBrowserName-->" + this.headlessBrowserName);

        if (GlobalConstants.HEADLESS_BROWSER_HTMLUNIT.equalsIgnoreCase(this.headlessBrowserName)) {
            webDriver = new HtmlUnitDriver(BrowserVersion.getDefault(), true) {
                @Override
                protected WebClient newWebClient(BrowserVersion version) {
                    WebClient webClient = super.newWebClient(version);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                    return webClient;
                }
            };
        } else if (GlobalConstants.HEADLESS_BROWSER_CHROME.equalsIgnoreCase(this.headlessBrowserName)) {
            if (GlobalConstants.TARGET_ENV_WINDOWS.equalsIgnoreCase(this.getTargetEnv())) {
                // TODO
            } else {
                System.setProperty("webdriver.chrome.driver", Utils.findFile("/chromedriver", this.getTargetEnv()));
            }
            ChromeOptions chromeOptions = new ChromeOptions();

            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("start-maximized");
            chromeOptions.addArguments("disable-infobars");
            chromeOptions.addArguments("--disable-extensions");

            // firefoxOptions.setHeadless(true);
            webDriver = new ChromeDriver(chromeOptions);
            webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        } else {

            DesiredCapabilities caps = getPhantomJSDesiredCapabilities();
            caps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36");
            String[] phantomArgs = new String[] { "--webdriver-loglevel=NONE" };
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);

            webDriver = new PhantomJSDriver(caps);
        }
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    @Override
    public void openNewWindowWithProxy(String proxyHost, String proxyServerPort, String proxyUsername, String proxyPassword) {

        logger.info("headlessBrowserName-->" + this.headlessBrowserName);

        if (GlobalConstants.HEADLESS_BROWSER_HTMLUNIT.equalsIgnoreCase(this.headlessBrowserName)) {
            ProxyConfig proxyConfig = new ProxyConfig(proxyHost, Integer.valueOf(proxyServerPort),null);            
            webDriver = new HtmlUnitDriver(BrowserVersion.getDefault(), true) {
                @Override
                protected WebClient newWebClient(BrowserVersion version) {
                    WebClient webClient = super.newWebClient(version);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                    webClient.getOptions().setProxyConfig(proxyConfig);
                    webClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new NTCredentials(proxyUsername, proxyPassword, "", ""));
                    return webClient;
                }
            };
        } else {

            DesiredCapabilities caps = getPhantomJSDesiredCapabilities();

            /*org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
            proxy.setHttpProxy(proxyServerIP + ":" + proxyServerPort);
            proxy.setSslProxy(proxyServerIP + ":" + proxyServerPort);
            proxy.setSocksUsername(proxyUsername);
            proxy.setSocksPassword(proxyPassword);
            caps.setCapability(CapabilityType.PROXY, proxy);*/

            List<String> cliArgsCap = new ArrayList<String>();
            cliArgsCap.add("--proxy=" + proxyHost + ":" + proxyServerPort);
            cliArgsCap.add("--proxy-auth=" + proxyUsername + ":" + proxyPassword);
            cliArgsCap.add("--proxy-type=http");
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
            caps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0");

            webDriver = new PhantomJSDriver(caps);
        }
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

    }

    private DesiredCapabilities getPhantomJSDesiredCapabilities() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        // caps.setCapability("takesScreenshot", true);
        if (GlobalConstants.TARGET_ENV_WINDOWS.equalsIgnoreCase(this.getTargetEnv())) {
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "bin/" + this.getTargetEnv() + "/phantomjs.exe");
        } else {

            File file = new File("bin/" + this.getTargetEnv() + "/phantomjs");

            file.setExecutable(true, false);
            file.setReadable(true, false);
            file.setWritable(true, false);

            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "bin/" + this.getTargetEnv() + "/phantomjs");
        }

        // caps.setCapability("takesScreenshot", true);

        return caps;
    }

}
