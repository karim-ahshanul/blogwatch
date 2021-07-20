package com.baeldung.site.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

public interface ITitleAnalyzerStrategy {
    boolean isTitleValid(String title, List<String> tokens, List<String> emphasizedAndItalicTokens);

    static List<ITitleAnalyzerStrategy> titleAnalyzerStrategies = Arrays.asList(new ITitleAnalyzerStrategy[] { articlesConjunctionsShortPrepositionsAnalyserStrategy(), javaMethodNameAnalyserStrategy(), simpleTitleAnalyserStrategy() });
    static String regexForShortPrepositions = "a|an|and|as|at|but|by|en|for|in|nor|of|on|or|per|the|vs.?|via|out";
    static String regexForExceptions = "with|to|from|up|into|v.?|REST|if|using";

    static ITitleAnalyzerStrategy articlesConjunctionsShortPrepositionsAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens) -> {
            String token = null;
            int firstTokenIndexStartingWithACharacter = Character.isDigit(Character.valueOf(title.charAt(0))) || ">".equals(String.valueOf(title.charAt(0)))? 1 : 0;
            String expectedToken = null;
            for (int j = 0; j < tokens.size(); j++) {
                token = tokens.get(j);
                if (emphasizedAndItalicTokens.contains(token.trim())) {
                    continue;
                }
                if (Pattern.compile(regexForShortPrepositions, Pattern.CASE_INSENSITIVE).matcher(token.trim()).matches()) {
                    if (j == firstTokenIndexStartingWithACharacter || j == tokens.size() - 1) {
                        expectedToken = WordUtils.capitalize(token.toLowerCase());
                    } else {
                        expectedToken = token.toLowerCase();
                    }
                    if (!expectedToken.equals(token)) {
                        return false;
                    }
                }
            }
            return true;
        };
    }

    static ITitleAnalyzerStrategy javaMethodNameAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens) -> {

            for (String token : tokens) {
                if (token.contains("(")) {
                    if (token.toUpperCase().equals(token)) {
                        continue;
                    }
                    if (token.contains(".")) {
                        String expetedToken = WordUtils.capitalize(Arrays.asList(token.split("\\.")).stream().map(WordUtils::uncapitalize).collect(Collectors.joining(".")));
                        if (!expetedToken.equals(token)) {
                            return false;
                        }
                    }
                    else if (token.contains("#")) {
                        String expetedToken = WordUtils.capitalize(Arrays.asList(token.split("#")).stream().map(WordUtils::uncapitalize).collect(Collectors.joining("#")));
                        if (!expetedToken.equals(token)) {
                            return false;
                        }
                    }
                    else if (token.contains("::")) {
                        String expetedToken = WordUtils.capitalize(Arrays.asList(token.split("::")).stream().map(WordUtils::uncapitalize).collect(Collectors.joining("::")));
                        if (!expetedToken.equals(token)) {
                            return false;
                        }
                    }                    
                    else {
                        if (!WordUtils.uncapitalize(token, '$').equals(token)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };
    }

    static ITitleAnalyzerStrategy simpleTitleAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens) -> {
            String token = null;
            int firstTokenIndexStartingWithACharacter = Character.isDigit(Character.valueOf(title.charAt(0))) || ">".equals(String.valueOf(title.charAt(0))) ? 1 : 0;
            for (int j = 0; j < tokens.size(); j++) {
                token = tokens.get(j);

                // ignore if not first and last word
                if (j != firstTokenIndexStartingWithACharacter && j != tokens.size() - 1 && Pattern.compile(regexForExceptions, Pattern.CASE_INSENSITIVE).matcher(token).matches()) {
                    continue;
                }

                if (Pattern.compile(regexForShortPrepositions, Pattern.CASE_INSENSITIVE).matcher(token).matches() || emphasizedAndItalicTokens.contains(token.trim()) || token.contains("(") || token.contains(".") || token.equals(token.toUpperCase())
                        || token.charAt(0) == '@' || (token.contains("-") && token.toLowerCase().equals(token))) {
                    continue;
                }

                if (!WordUtils.capitalize(token).equals(token)) {
                    return false;
                }
            }

            return true;
        };
    }
}
