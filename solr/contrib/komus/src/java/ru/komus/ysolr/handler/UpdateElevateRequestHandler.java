package ru.komus.ysolr.handler;

import de.hybris.platform.solrfacetsearch.ysolr.event.impl.EventDispatcher;
import de.hybris.platform.solrfacetsearch.ysolr.exceptions.util.ExceptionUtils;
import org.apache.solr.handler.StandardRequestHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.komus.ysolr.elevate.ElevateData;
import ru.komus.ysolr.event.UpdateElevateEvent;

public class UpdateElevateRequestHandler extends StandardRequestHandler {
    public static final Logger LOG = LoggerFactory.getLogger(de.hybris.platform.solrfacetsearch.ysolr.handlers.UpdateSynonymsRequestHandler.class);

    public UpdateElevateRequestHandler() {
    }

    public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
        LOG.debug("Updating Elevate Queries...");

        try {
            String elevateXml = req.getParams().get("elevateXml");
            EventDispatcher.dispatch(new UpdateElevateEvent(this, new ElevateData(elevateXml), req.getCore()));
        } catch (Exception var6) {
            LOG.error("Elevate Queries could not be updated", var6);
            rsp.setException(var6);
            rsp.add("status", "ERROR");
            rsp.add("ERROR_EXCEPTION", ExceptionUtils.getStackTraceAsString(var6));
        }

        rsp.setHttpCaching(true);
    }

}

