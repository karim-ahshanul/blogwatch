package com.baeldung.jsoup;

import com.baeldung.common.dto.DependencyDto;
import com.baeldung.common.dto.DependencyVersionDto;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class ArticleDependencyVersionExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleDependencyVersionExtractor.class);

    public List<DependencyVersionDto> extractDependencyVersion(DependencyDto searchedDependency, URL article) {
        try {
            return Jsoup.parse(article, 10000)
              .getElementsByTag("code")
              .stream()
              .filter(this::isXmlCode)
              .map(this::xmlContent)
              .map(this::toDocument)
              .peek(document -> {if (document == null) System.out.println("Couldn't parse some XML in this article: " + article);})
              .filter(Objects::nonNull)
              .flatMap(this::toDependencies)
              .filter(dependencyVersion -> dependencyVersion.getDependency().sameArtifactAs(searchedDependency))
              .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while reading URL: " + article);
        }
    }

    private boolean isXmlCode(Element element) {
        return element.attr("class").contains("language-xml");
    }

    private String xmlContent(Element element) {
        return normalize(unescape(rawXml(element)));
    }

    private String normalize(String unescapedXml) {
        return "<root>" + unescapedXml + "</root>";
    }

    private String unescape(String rawXml) {
        return StringEscapeUtils.unescapeXml(rawXml).replaceAll("&nbsp;", "");
    }

    private String rawXml(Element element) {
        return element.childNode(0).toString();
    }

    private Document toDocument(String xmlString) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }

    private Stream<DependencyVersionDto> toDependencies(Document document) {
        return Stream.concat(
          extractDependenciesFromTag(document, "dependency"),
          extractDependenciesFromTag(document, "parent")
        );
    }

    private Stream<DependencyVersionDto> extractDependenciesFromTag(Document document, String tag) {
        NodeList tagNodes = document.getElementsByTagName(tag);
        return IntStream
          .range(0, tagNodes.getLength())
          .mapToObj(tagNodes::item)
          .map(this::extractDependency);
    }

    private DependencyVersionDto extractDependency(Node node) {
        return new DependencyVersionDto(
          new DependencyDto(
            extractGroupId(node),
            extractArtifactId(node)
          ),
          extractVersion(node)
        );
    }

    private String extractGroupId(Node dependencyNode) {
        return extractTag(dependencyNode, "groupId");
    }

    private String extractTag(Node dependencyNode, String tag) {
        for (int node = 0; node < dependencyNode.getChildNodes().getLength(); node++) {
            if (nthChildNode(dependencyNode, node).getNodeName().equals(tag)) {
                return nthChildNode(dependencyNode, node).getTextContent();
            }
        }

        return null;
    }

    private String extractArtifactId(Node dependencyNode) {
        return extractTag(dependencyNode, "artifactId");
    }

    private String extractVersion(Node dependencyNode) {
        return extractTag(dependencyNode, "version");
    }

    private Node nthChildNode(Node dependencyNode, int nth) {
        return dependencyNode.getChildNodes().item(nth);
    }
}
