package ru.komus.ysolr.synonyms;

import de.hybris.platform.solrfacetsearch.ysolr.synonyms.HybrisSynonymFilterFactory;
import org.apache.solr.core.SolrResourceLoader;

import java.io.File;
import java.util.Map;

public class SynonymFilterFactory extends HybrisSynonymFilterFactory {
    public SynonymFilterFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    protected File getDataFile(SolrResourceLoader loader, String filename) {
        String dir = loader.getConfigDir();
        return dir == null ? null : new File(dir + File.separator + "ydata" + File.separator + filename + this.langCode + ".txt");

    }
}