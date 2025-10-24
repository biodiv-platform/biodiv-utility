package com.strandls.utility.pojo;

import java.util.List;
import java.util.Map;

public class SpeciesDownload {

	private String title;
	private String badge;
	private List<String> synonyms;
	private List<BreadCrumb> taxonomy;
	private Map<String, List<String>> commonNames;
	private List<String> conceptNames;
	private List<SpeciesField> fieldData;

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

}