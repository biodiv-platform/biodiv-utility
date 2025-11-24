package com.strandls.utility.pojo;

import java.util.List;

public class FieldValue {
	private String description;
	private Long languageId;
	private String attributions;
	private String license;
	private List<String> contributor;

	public FieldValue() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	public String getAttributions() {
		return attributions;
	}

	public void setAttributions(String attributions) {
		this.attributions = attributions;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public List<String> getContributor() {
		return contributor;
	}

	public void setContributor(List<String> contributor) {
		this.contributor = contributor;
	}
}