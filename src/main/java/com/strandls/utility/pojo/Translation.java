package com.strandls.utility.pojo;

public class Translation {

	private Long id;
	private String title;
	private Long languageId;
	private String description;
	private String readMoreText;

	/**
	 * 
	 */
	public Translation() {
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
	public Translation(Long id, String title, Long languageId, String description, String readMoreText) {
		super();
		this.id = id;
		this.title = title;
		this.languageId = languageId;
		this.description = description;
		this.readMoreText = readMoreText;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReadMoreText() {
		return readMoreText;
	}

	public void setReadMoreText(String readMoreText) {
		this.readMoreText = readMoreText;
	}

}