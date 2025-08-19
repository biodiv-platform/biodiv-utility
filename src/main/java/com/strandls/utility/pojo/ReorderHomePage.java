/** */
package com.strandls.utility.pojo;

public class ReorderHomePage {

	private Long displayOrder;
	private Long galleryId;

	/** */
	public ReorderHomePage() {
		super();
	}

	/**
	 * @param galleryId
	 * @param displayOrder
	 */
	public ReorderHomePage(Long galleryId, Long displayOrder) {
		super();
		this.displayOrder = displayOrder;
		this.galleryId = galleryId;
	}

	public Long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Long getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(Long galleryId) {
		this.galleryId = galleryId;
	}
}
