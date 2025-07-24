/**
 * 
 */
package com.strandls.utility.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Mekala Rishitha Ravi
 *
 */

@Entity
@Table(name = "mini_gallery_slider")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiniGallerySlider {

	private Long id;
	private String fileName;
	private Long observationId;
	private Long authorId;
	private String authorName;
	private String authorImage;
	private String title;
	private String customDescripition;
	private String moreLinks;
	private Long displayOrder;
	private Boolean isTruncated;
	private String readMoreText;
	private String readMoreUIType;
	private Long galleryId;
	private Long languageId;
	private String color;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "BIGINT")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "observation_id", columnDefinition = "BIGINT")
	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	@Column(name = "author_id", columnDefinition = "BIGINT")
	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	@Transient
	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	@Transient
	public String getAuthorImage() {
		return authorImage;
	}

	public void setAuthorImage(String authorImage) {
		this.authorImage = authorImage;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "custom_desc", columnDefinition = "TEXT")
	public String getCustomDescripition() {
		return customDescripition;
	}

	public void setCustomDescripition(String customDescripition) {
		this.customDescripition = customDescripition;
	}

	@Column(name = "more_links")
	public String getMoreLinks() {
		return moreLinks;
	}

	public void setMoreLinks(String moreLinks) {
		this.moreLinks = moreLinks;
	}

	@Column(name = "display_order", nullable = false, columnDefinition = "BIGINT")
	public Long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "is_truncated", nullable = false, columnDefinition = "BOOLEAN")
	public Boolean getTruncated() {
		return isTruncated;
	}

	public void setTruncated(Boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	@Column(name = "read_more_text", columnDefinition = "TEXT")
	public String getReadMoreText() {
		return readMoreText;
	}

	public void setReadMoreText(String readMoreText) {
		this.readMoreText = readMoreText;
	}

	@Column(name = "read_more_ui_type", columnDefinition = "text default 'button'")
	public String getReadMoreUIType() {
		return readMoreUIType;
	}

	public void setReadMoreUIType(String readMoreUIType) {
		this.readMoreUIType = readMoreUIType;
	}
	
	@Column(name = "gallery_id", columnDefinition = "BIGINT")
	public Long getGalleryId() {
		return galleryId;
	}

	public void setGalleryId(Long galleryId) {
		this.galleryId = galleryId;
	}

	@Column(name = "language_id", columnDefinition = "BIGINT")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}
	
	@Column(name = "color")
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
