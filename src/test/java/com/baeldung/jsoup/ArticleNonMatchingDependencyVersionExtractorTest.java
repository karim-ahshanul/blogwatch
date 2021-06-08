package com.baeldung.jsoup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baeldung.common.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.baeldung.common.dto.DependencyDto;
import com.baeldung.common.dto.DependencyVersionDto;
import com.baeldung.jsoup.config.JSoupMainConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JSoupMainConfig.class)
class ArticleNonMatchingDependencyVersionExtractorTest {
    protected Logger logger = LoggerFactory.getLogger(getClass()); 
    
    private final ModuleArticleUrlsExtractor moduleArticleUrlsExtractor;
    private final ArticleNonMatchingDependencyVersionExtractor articleNonMatchingDependencyVersionExtractor;
    private final String searchedModules;
    private final DependencyVersionDto searchedDependencyVersion;

    @Autowired
    ArticleNonMatchingDependencyVersionExtractorTest(
      ModuleArticleUrlsExtractor moduleArticleUrlsExtractor,
      ArticleNonMatchingDependencyVersionExtractor articleNonMatchingDependencyVersionExtractor,
      @Value("${modules:https://github.com/eugenp/tutorials/tree/master/akka-http}") String searchedModules,
      @Value("${artifactId:akka-http-jackson_2.12}") String searchedArtifactId,
      @Value("${version:10.1.12}") String searchedVersion) {
        this.moduleArticleUrlsExtractor = moduleArticleUrlsExtractor;
        this.articleNonMatchingDependencyVersionExtractor = articleNonMatchingDependencyVersionExtractor;
        this.searchedModules = searchedModules;
        this.searchedDependencyVersion = new DependencyVersionDto(new DependencyDto(searchedArtifactId), searchedVersion);
    }

    @Test
    void givenModulesAndAMavenDependency_whenAnalysingArticles_thenArticlesMatchingTheMavenDependencyReturned() {
        List<URL> moduleToExtractUrls = Arrays.stream(searchedModules.split("\r?\n"))
          .map(this::toUrl)
          .collect(Collectors.toList());

        Set<URL> articleUrlsInModules = moduleArticleUrlsExtractor.findArticleUrlsInModules(moduleToExtractUrls);

        Map<URL, List<DependencyVersionDto>> notMatchingDependenciesByArticle = articleUrlsInModules
          .stream()
          .collect(Collectors.toMap(
            Function.identity(),
            articleUrl -> {
                try {
                    return articleNonMatchingDependencyVersionExtractor.extractNonMatchingDependencyVersions(searchedDependencyVersion, new URL (Utils.changeLiveUrlWithStaging8(articleUrl.toString())));
                } catch (MalformedURLException e) {
                    logger.info("Error while converting live url to staging8. url: {}", articleUrl.toString());
                    e.printStackTrace();
                    return null;                    
                }
            }
          )).entrySet()
          .stream()
          .filter(entry -> !entry.getValue().isEmpty())
          .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
          ));

        Utils.printItems(
          "Considered articles",
          articleUrlsInModules,
          articleUrl -> "- " + articleUrl
        );

        Utils.printItems(
          "Found dependencies not matching the given version",
          notMatchingDependenciesByArticle.entrySet(),
          dependenciesByArticleUrl -> "- " + dependenciesByArticleUrl.getKey() + ": " + dependenciesByArticleUrl.getValue()
        );
    }

    private URL toUrl(String moduleUrl) {
        try {
            return new URL(moduleUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("The module URL couldn't be parsed: " + moduleUrl);
        }
    }
}