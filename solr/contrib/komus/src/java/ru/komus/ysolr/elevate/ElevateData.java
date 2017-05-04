package ru.komus.ysolr.elevate;

import de.hybris.platform.solrfacetsearch.ysolr.synonyms.data.LanguageData;

public class ElevateData implements LanguageData {
    private final String elevateXml;

    public ElevateData(String elevateXml) {
        this.elevateXml = elevateXml;
    }

    @Override
    public String getLang() {
        return null;
    }

    public String getElevateXml() {
        return elevateXml;
    }
}
