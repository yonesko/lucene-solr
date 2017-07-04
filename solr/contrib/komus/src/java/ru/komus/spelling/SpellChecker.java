package ru.komus.spelling;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.solr.spelling.AbstractLuceneSpellChecker;
import org.apache.solr.spelling.IndexBasedSpellChecker;
import org.apache.solr.spelling.SpellingOptions;
import org.apache.solr.spelling.SpellingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpellChecker extends IndexBasedSpellChecker {
    @Override
    public SpellingResult getSuggestions(SpellingOptions options) throws IOException {
        SpellingResult result = new SpellingResult(options.tokens);
        IndexReader reader = determineReader(options.reader);
        Term term = field != null ? new Term(field, "") : null;
        float theAccuracy = (options.accuracy == Float.MIN_VALUE) ? spellChecker.getAccuracy() : options.accuracy;

        int count = Math.max(options.count, AbstractLuceneSpellChecker.DEFAULT_SUGGESTION_COUNT);
        for (Token token : options.tokens) {
            String tokenText = new String(token.buffer(), 0, token.length());
            term = new Term(field, tokenText);
            int docFreq = 0;
            if (reader != null) {
                docFreq = reader.docFreq(term);
            }
            String[] suggestions = spellChecker.suggestSimilar(tokenText,
                    ((options.alternativeTermCount == null || docFreq == 0) ? count
                            : options.alternativeTermCount), field != null ? reader : null, // workaround LUCENE-1295
                    field, options.suggestMode, theAccuracy);
            if (suggestions.length == 1 && suggestions[0].equals(tokenText)
                    && options.alternativeTermCount == null) {
                // These are spelled the same, continue on
                continue;
            }
            // If considering alternatives to "correctly-spelled" terms, then add the
            // original as a viable suggestion.
            if (options.alternativeTermCount != null && docFreq > 0) {
                boolean foundOriginal = false;
                String[] suggestionsWithOrig = new String[suggestions.length + 1];
                for (int i = 0; i < suggestions.length; i++) {
                    if (suggestions[i].equals(tokenText)) {
                        foundOriginal = true;
                        break;
                    }
                    suggestionsWithOrig[i + 1] = suggestions[i];
                }
                if (!foundOriginal) {
                    suggestionsWithOrig[0] = tokenText;
                    suggestions = suggestionsWithOrig;
                }
            }

            if (options.extendedResults == true && reader != null && field != null) {
                result.addFrequency(token, docFreq);
                int countLimit = Math.min(options.count, suggestions.length);
                if(countLimit>0)
                {
                    for (int i = 0; i < countLimit; i++) {
                        term = new Term(field, suggestions[i]);
                        result.add(token, suggestions[i], reader.docFreq(term));
                    }
                } else {
                    List<String> suggList = Collections.emptyList();
                    result.add(token, suggList);
                }
            } else {
                if (suggestions.length > 0) {
                    List<String> suggList = Arrays.asList(suggestions);
                    if (suggestions.length > options.count) {
                        suggList = suggList.subList(0, options.count);
                    }
                    result.add(token, suggList);
                } else {
                    List<String> suggList = Collections.emptyList();
                    result.add(token, suggList);
                }
            }
        }
        return result;

    }
}
