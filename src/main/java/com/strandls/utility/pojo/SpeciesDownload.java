package com.strandls.utility.pojo;

import java.util.List;
import java.util.Map;

public class SpeciesDownload {

	private String title;
	private String speciesGroup;
	private String badge;
	private List<String> synonyms;
	private List<BreadCrumb> taxonomy;
	private Map<String, List<String>> commonNames;
	private List<String> conceptNames;
	private List<SpeciesField> fieldData;
	private Map<String, List<String>> references;
	private String chartImage;
	private String traitsChart;
	private String observationMap;
	private List<String> resourceData;
	private List<DocumentMeta> documentMetaList;

	/**
	 * 
	 */
	public SpeciesDownload() {
		super();
	}

	/**
	 * @param species
	 * @param observation
	 * @param maps
	 * @param documents
	 * @param discussions
	 * @param activeUser
	 */

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSpeciesGroup() {
		return speciesGroup;
	}

	public void setSpeciesGroup(String speciesGroup) {
		this.speciesGroup = speciesGroup;
	}
	
	public String getBadge() {
		return badge;
	}

	public void setBadge(String badge) {
		this.badge = badge;
	}
	
	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}
	
	public List<BreadCrumb> getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(List<BreadCrumb> taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public Map<String, List<String>> getCommonNames() {
		return commonNames;
	}

	public void setCommonNames(Map<String, List<String>> commonNames) {
		this.commonNames = commonNames;
	}
	
	public List<String> getConceptNames() {
		return conceptNames;
	}

	public void setConceptNames(List<String> conceptNames) {
		this.conceptNames = conceptNames;
	}
	
	public List<SpeciesField> getFieldData() {
		return fieldData;
	}

	public void setFieldData(List<SpeciesField> fieldData) {
		this.fieldData = fieldData;
	}

	public Map<String, List<String>> getReferences() {
		return references;
	}

	public void setReferences(Map<String, List<String>> references) {
		this.references = references;
	}
	
	public String getChartImage() {
        return chartImage;
    }
    
    public void setChartImage(String chartImage) {
        this.chartImage = chartImage;
    }
    
    public String getTraitsChart() {
        return traitsChart;
    }
    
    public void setTraitsChart(String traitsChart) {
        this.traitsChart = traitsChart;
    }
    
    public String getObservationMap() {
        return observationMap;
    }
    
    public void setObservationMap(String observationMap) {
        this.observationMap = observationMap;
    }
    
    public List<String> getResourceData() {
        return resourceData;
    }
    
    public void setResourceData(List<String> resourceData) {
        this.resourceData = resourceData;
    }
    
    public List<DocumentMeta> getDocumentMetaList() {
        return documentMetaList;
    }
    
    public void setDocumentMetaList(List<DocumentMeta> documentMetaList) {
        this.documentMetaList = documentMetaList;
    }
}