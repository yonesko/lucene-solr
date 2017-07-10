package ru.komus.spelling;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.HighFrequencyDictionary;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


/**
 * <p>
 * A spell checker implementation that loads words from Solr as well as arbitary Lucene indices.
 * </p>
 * <p>
 * <p>
 * Refer to <a href="http://wiki.apache.org/solr/SpellCheckComponent">SpellCheckComponent</a>
 * for more details.
 * </p>
 *
 * @since solr 1.3
 **/
public class IndexBasedSpellChecker extends AbstractLuceneSpellChecker {
    private static final Logger log = LoggerFactory.getLogger(org.apache.solr.spelling.IndexBasedSpellChecker.class);

    public static final String THRESHOLD_TOKEN_FREQUENCY = "thresholdTokenFrequency";

    protected float threshold;
    protected IndexReader reader;

    @Override
    public String init(NamedList config, SolrCore core) {
        super.init(config, core);
        threshold = config.get(THRESHOLD_TOKEN_FREQUENCY) == null ? 0.0f
                : (Float) config.get(THRESHOLD_TOKEN_FREQUENCY);
        initSourceReader();
        return name;
    }

    private void initSourceReader() {
        if (sourceLocation != null) {
            try {
                FSDirectory luceneIndexDir = FSDirectory.open(new File(sourceLocation));
                this.reader = DirectoryReader.open(luceneIndexDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void build(SolrCore core, SolrIndexSearcher searcher) throws IOException {
        IndexReader reader = null;
        if (sourceLocation == null) {
            // Load from Solr's index
            reader = searcher.getIndexReader();
        } else {
            // Load from Lucene index at given sourceLocation
            reader = this.reader;
        }

        // Create the dictionary
        dictionary = new HighFrequencyDictionary(reader, field,
                threshold);
        // TODO: maybe whether or not to clear the index should be configurable?
        // an incremental update is faster (just adds new terms), but if you 'expunged'
        // old terms I think they might hang around.
        spellChecker.clearIndex();
        // TODO: you should be able to specify the IWC params?
        // TODO: if we enable this, codec gets angry since field won't exist in the schema
        // config.setCodec(core.getCodec());
        spellChecker.indexDictionary(dictionary, new IndexWriterConfig(core.getSolrConfig().luceneMatchVersion, null), false);
    }

    @Override
    protected IndexReader determineReader(IndexReader reader) {
        IndexReader result = null;
        if (sourceLocation != null) {
            result = this.reader;
        } else {
            result = reader;
        }
        return result;
    }

    @Override
    public void reload(SolrCore core, SolrIndexSearcher searcher) throws IOException {
        super.reload(core, searcher);
        //reload the source
        initSourceReader();
    }

    public float getThreshold() {
        return threshold;
    }
}
