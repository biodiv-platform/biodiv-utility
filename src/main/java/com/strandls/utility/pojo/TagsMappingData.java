package com.strandls.utility.pojo;

import com.strandls.activity.pojo.MailData;

public class TagsMappingData {

	private TagsMapping tagsMapping;
	private MailData mailData;

	/** */
	public TagsMappingData() {
		super();
	}

	/**
	 * @param tagsMapping
	 * @param mailData
	 */
	public TagsMappingData(TagsMapping tagsMapping, MailData mailData) {
		super();
		this.tagsMapping = tagsMapping;
		this.mailData = mailData;
	}

	public TagsMapping getTagsMapping() {
		return tagsMapping;
	}

	public void setTagsMapping(TagsMapping tagsMapping) {
		this.tagsMapping = tagsMapping;
	}

	public MailData getMailData() {
		return mailData;
	}

	public void setMailData(MailData mailData) {
		this.mailData = mailData;
	}
}
