package com.baeldung.jsoup;

import com.baeldung.common.dto.DependencyVersionDto;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleNonMatchingDependencyVersionExtractor {
    private final ArticleDependencyVersionExtractor articleDependencyVersionExtractor;

    public ArticleNonMatchingDependencyVersionExtractor(ArticleDependencyVersionExtractor articleDependencyVersionExtractor) {
        this.articleDependencyVersionExtractor = articleDependencyVersionExtractor;
    }

    public List<DependencyVersionDto> extractNonMatchingDependencyVersions(DependencyVersionDto searchedDependencyVersion, URL article) {
        return articleDependencyVersionExtractor.extractDependencyVersion(searchedDependencyVersion.getDependency(), article)
          .stream()
          .filter(dependencyVersion -> !searchedDependencyVersion.getVersion().equals(dependencyVersion.getVersion()))
          .collect(Collectors.toList());
    }
}
