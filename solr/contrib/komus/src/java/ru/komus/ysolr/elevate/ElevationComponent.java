package ru.komus.ysolr.elevate;


import de.hybris.platform.solrfacetsearch.ysolr.event.HybrisEventListener;
import de.hybris.platform.solrfacetsearch.ysolr.event.impl.EventDispatcher;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.QueryElevationComponent;
import ru.komus.ysolr.event.UpdateElevateEvent;

import java.io.File;

public class ElevationComponent extends QueryElevationComponent {
    static final String CONFIG_FILE = "config-file";
    private SolrParams solrParams;

    @Override
    public void init(final NamedList args) {
        super.init(args);

        solrParams = SolrParams.toSolrParams(args);
    }

    @Override
    public void inform(final SolrCore core) {
        super.inform(core);

        EventDispatcher.registerListener(new HybrisEventListener<UpdateElevateEvent>() {
            @Override
            public Class<UpdateElevateEvent> getAcceptedType() {
                return UpdateElevateEvent.class;
            }

            @Override
            public void onEvent(UpdateElevateEvent event) throws Exception {
                String f = solrParams.get(CONFIG_FILE);
                if (f == null) {
                    throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                            "QueryElevationComponent must specify argument: " + CONFIG_FILE);
                }

                File fC = new File(core.getResourceLoader().getConfigDir(), f);

                String elevateXml = event.getData().getElevateXml();

                if (StringUtils.isNotEmpty(elevateXml)) {
                    FileUtils.write(fC, elevateXml);
                }
            }
        });
    }
}
