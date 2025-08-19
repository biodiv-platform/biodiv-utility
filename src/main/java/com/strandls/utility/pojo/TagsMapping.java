/** */
package com.strandls.utility.pojo;

import java.util.List;

/**
 * @author Abhishek Rudra
 */
public class TagsMapping {

	private Long objectId;
	private List<Tags> tags;

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public List<Tags> getTags() {
		return tags;
	}

	public void setTags(List<Tags> tags) {
		this.tags = tags;
	}
}
