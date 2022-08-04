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
 * @author Abhishek Rudra
 *
 */

@Entity
@Table(name = "gallery_slider")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GallerySlider {

	private Long id;
	private Long ugId;
	private String fileName;
	private Long observationId;
	private Long authorId;
	private String authorName;
	private String authorImage;
	private String title;
	private String customDescripition;
	private String moreLinks;
	private Long displayOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ug_id")
	public Long getUgId() {
		return ugId;
	}

	public void setUgId(Long ugId) {
		this.ugId = ugId;
	}

	@Column(name = "file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "observation_id")
	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	@Column(name = "author_id")
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

	@Column(name = "display_order", nullable = false)
	public Long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

}
