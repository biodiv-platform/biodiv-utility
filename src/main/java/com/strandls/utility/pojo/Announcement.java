/**
 * 
 */
package com.strandls.utility.pojo;

import java.util.List;
import java.util.Map;

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
@Table(name = "announcement")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Announcement {

	private Long id;
	private String description;
	private Boolean enabled;
	private Long announcementId;
	private Long languageId;
	private String color;
	private String bgColor;
	private Map<Long, String> translations;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "BIGINT")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "description", columnDefinition = "TEXT")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN")
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	@Column(name = "announcement_id", columnDefinition = "BIGINT")
	public Long getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(Long announcementId) {
		this.announcementId = announcementId;
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
	
	@Column(name = "bg_color")
	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}
	
	@Transient
	public Map<Long, String> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<Long, String> translations) {
		this.translations = translations;
	}
}