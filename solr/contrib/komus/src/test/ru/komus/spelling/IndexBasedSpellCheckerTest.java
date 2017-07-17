package ru.komus.spelling;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.spelling.SolrSpellChecker;
import org.apache.solr.spelling.SpellingOptions;
import org.apache.solr.spelling.SpellingResult;
import org.apache.solr.util.RefCounted;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

import static ru.komus.util.LayoutSwitcher.doSwitch;

public class IndexBasedSpellCheckerTest extends SolrTestCaseJ4 {
    private static IndexBasedSpellChecker checker = new IndexBasedSpellChecker();
    private static RefCounted<SolrIndexSearcher> holder;
    private static SolrIndexSearcher searcher;
    private static IndexReader reader;

    private static String[] DOCS = new String[]{
            "This is a title",
            "The quick reb fox jumped over the lazy brown dogs.",
            "This is a document",
            "another document",
            "red fox",
            "green bun",
            "green bud"
    };
    private static SolrCore core;


    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("komus-solrconfig.xml", "komus-schema.xml");
        core = h.getCore();
        //Index something with a title
        Scanner sc = new Scanner(IndexBasedSpellCheckerTest.class.getResourceAsStream("/name_text_ru.txt"));
        int i = 0;
        while (sc.hasNext())
            assertNull(h.validateUpdate(adoc("id", String.valueOf(i++), "spellcheck_ru", sc.next())));
        for (String doc : DOCS)
            assertNull(h.validateUpdate(adoc("id", String.valueOf(i++), "spellcheck_ru", doc)));
        assertNull(h.validateUpdate(commit()));

        NamedList<Object> spellchecker = new NamedList();
        spellchecker.add("classname", IndexBasedSpellChecker.class.getName());

        File indexDir = new File(TEMP_DIR, "spellingIdx" + new Date().getTime());
        assertTrue(indexDir.mkdirs());
        spellchecker.add(org.apache.solr.spelling.AbstractLuceneSpellChecker.INDEX_DIR, indexDir.getAbsolutePath());
        spellchecker.add(org.apache.solr.spelling.AbstractLuceneSpellChecker.FIELD, "spellcheck_ru");
        spellchecker.add(org.apache.solr.spelling.AbstractLuceneSpellChecker.SPELLCHECKER_ARG_NAME, spellchecker);

        String dictName = checker.init(spellchecker, core);
        assertTrue(dictName + " is not equal to " + SolrSpellChecker.DEFAULT_DICTIONARY_NAME,
                dictName.equals(SolrSpellChecker.DEFAULT_DICTIONARY_NAME));

        holder = core.getSearcher();
        searcher = holder.get();
        checker.build(core, searcher);
        reader = searcher.getIndexReader();
    }

    @Test
    public void layoutAndTypo() throws Exception {
        try {
            SpellingOptions spellOpts = new SpellingOptions(getTokens(doSwitch("карандашей"), checker.getQueryAnalyzer()), reader);
            SpellingResult result = checker.getSuggestions(spellOpts);

            assertTrue("result is null and it shouldn't be", result != null);
            Map<String, Integer> suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("documemt is null and it shouldn't be", suggestions != null);
            assertTrue("documemt Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
            Map.Entry<String, Integer> entry = suggestions.entrySet().iterator().next();
            assertTrue(entry.getKey() + " is not equal to " + "document", entry.getKey().equals("карандашей"));
            assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);


            spellOpts = new SpellingOptions(getTokens(doSwitch("Colotech"), checker.getQueryAnalyzer()), reader);
            result = checker.getSuggestions(spellOpts);

            assertTrue("result is null and it shouldn't be", result != null);
            suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("documemt is null and it shouldn't be", suggestions != null);
            assertTrue("documemt Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
            entry = suggestions.entrySet().iterator().next();
            assertTrue(entry.getKey() + " is not equal to " + "Colotech", entry.getKey().equalsIgnoreCase("Colotech"));
            assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);
        } finally {
            holder.decref();
        }

    }

    @Test
    public void originalTestSpelling() throws Exception {
        try {
            Collection<Token> tokens = getTokens("documemt", checker.getQueryAnalyzer());
            SpellingOptions spellOpts = new SpellingOptions(tokens, reader);
            SpellingResult result = checker.getSuggestions(spellOpts);
            assertTrue("result is null and it shouldn't be", result != null);
            //should be lowercased, b/c we are using a lowercasing analyzer
            Map<String, Integer> suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("documemt is null and it shouldn't be", suggestions != null);
            assertTrue("documemt Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
            Map.Entry<String, Integer> entry = suggestions.entrySet().iterator().next();
            assertTrue(entry.getKey() + " is not equal to " + "document", entry.getKey().equals("document"));
            assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);

            //test something not in the spell checker
            spellOpts.tokens = getTokens("super", checker.getQueryAnalyzer());
            result = checker.getSuggestions(spellOpts);
            assertTrue("result is null and it shouldn't be", result != null);
            suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("suggestions size should be 0", suggestions.size() == 0);

            //test something that is spelled correctly
            spellOpts.tokens = getTokens("document", checker.getQueryAnalyzer());
            result = checker.getSuggestions(spellOpts);
            assertTrue("result is null and it shouldn't be", result != null);
            suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("suggestions is null and it shouldn't be", suggestions == null);

            //Has multiple possibilities, but the exact exists, so that should be returned
            spellOpts.tokens = getTokens("red", checker.getQueryAnalyzer());
            spellOpts.count = 2;
            result = checker.getSuggestions(spellOpts);
            assertNotNull(result);
            suggestions = result.get(spellOpts.tokens.iterator().next());
            assertTrue("suggestions is not null and it should be", suggestions == null);

            //Try out something which should have multiple suggestions
            spellOpts.tokens = getTokens("bug", checker.getQueryAnalyzer());
            result = checker.getSuggestions(spellOpts);
            assertNotNull(result);
            suggestions = result.get(spellOpts.tokens.iterator().next());
            assertNotNull(suggestions);
            assertTrue("suggestions Size: " + suggestions.size() + " is not: " + 2, suggestions.size() == 2);
            entry = suggestions.entrySet().iterator().next();
            assertTrue(entry.getKey() + " is equal to " + "bug and it shouldn't be", !entry.getKey().equals("bug"));
            assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);

            entry = suggestions.entrySet().iterator().next();
            assertTrue(entry.getKey() + " is equal to " + "bug and it shouldn't be", !entry.getKey().equals("bug"));
            assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);
        } finally {
            holder.decref();
        }
    }

    private Collection<Token> getTokens(String q, Analyzer analyzer) throws IOException {
        Collection<Token> result = new ArrayList<Token>();
        assert analyzer != null;
        TokenStream ts = analyzer.tokenStream("", q);
        try {
            ts.reset();
            // TODO: support custom attributes
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
            TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);
            FlagsAttribute flagsAtt = ts.addAttribute(FlagsAttribute.class);
            PayloadAttribute payloadAtt = ts.addAttribute(PayloadAttribute.class);
            PositionIncrementAttribute posIncAtt = ts.addAttribute(PositionIncrementAttribute.class);

            while (ts.incrementToken()) {
                Token token = new Token();
                token.copyBuffer(termAtt.buffer(), 0, termAtt.length());
                token.setOffset(offsetAtt.startOffset(), offsetAtt.endOffset());
                token.setType(typeAtt.type());
                token.setFlags(flagsAtt.getFlags());
                token.setPayload(payloadAtt.getPayload());
                token.setPositionIncrement(posIncAtt.getPositionIncrement());
                result.add(token);
            }
            ts.end();
            return result;
        } finally {
            IOUtils.closeWhileHandlingException(ts);
        }
    }
}