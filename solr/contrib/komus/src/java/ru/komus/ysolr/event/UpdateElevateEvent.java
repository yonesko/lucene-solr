package ru.komus.ysolr.event;

import de.hybris.platform.solrfacetsearch.ysolr.event.AbstractHybrisEvent;
import org.apache.solr.core.SolrCore;
import ru.komus.ysolr.elevate.ElevateData;

public class UpdateElevateEvent extends AbstractHybrisEvent<ElevateData> {
    public UpdateElevateEvent(Object source, ElevateData data, SolrCore core) {
        super(source, data, core);
    }
}
