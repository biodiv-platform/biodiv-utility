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
 * @author Abhishek Rudra
 *
 */
@Entity
@Table(name = "home_page_data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomePageData {

	private Long id;
	private Boolean showGallery;
	private Boolean showStats;
	private Boolean showRecentObservation;
	private Boolean showGridMap;
	private Boolean showPartners;
	private Boolean showSponsors;
	private Boolean showDonors;
	private Boolean showDesc;

	private HomePageStats stats;
	private List<GallerySlider> gallerySlider;
	private List<GalleryConfig> miniGallery;
	private List<Map<String, Map<Long, List<MiniGallerySlider>>>> miniGallerySlider;
	private String description;
	private String ugDescription;

	private String title;
	private String siteLogo;
	private String favIcon;
	private Long languageId;
	private List<Translation> translations;

	/**
	 * 
	 */
	public HomePageData() {
		super();
	}

	/**
	 * @param showGallery
	 * @param showStats
	 * @param showRecentObservation
	 * @param showGridMap
	 * @param showPartners
	 * @param stats
	 * @param gallerySlider
	 * @param ugDescription
	 */
	public HomePageData(Long id, Boolean showGallery, Boolean showStats, Boolean showRecentObservation,
			Boolean showGridMap, Boolean showPartners, Boolean showSponsors, Boolean showDonors, Boolean showDesc,
			HomePageStats stats, List<GallerySlider> gallerySlider, String ugDescription, String description,
			List<GalleryConfig> miniGallery, List<Map<String, Map<Long, List<MiniGallerySlider>>>> miniGallerySlider,
			String title, String siteLogo, String favIcon, Long languageId, List<Translation> translations) {
		super();
		this.id = id;
		this.showGallery = showGallery;
		this.showStats = showStats;
		this.showRecentObservation = showRecentObservation;
		this.showGridMap = showGridMap;
		this.showPartners = showPartners;
		this.showSponsors = showSponsors;
		this.showDonors = showDonors;
		this.showDesc = showDesc;
		this.stats = stats;
		this.gallerySlider = gallerySlider;
		this.miniGallery = miniGallery;
		this.ugDescription = ugDescription;
		this.description = description;
		this.miniGallerySlider = miniGallerySlider;
		this.title = title;
		this.siteLogo = siteLogo;
		this.favIcon = favIcon;
		this.languageId = languageId;
		this.translations = translations;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "show_gallery", columnDefinition = "boolean default true")
	public Boolean getShowGallery() {
		return showGallery;
	}

	public void setShowGallery(Boolean showGallery) {
		this.showGallery = showGallery;
	}

	@Column(name = "show_stats", columnDefinition = "boolean default true")
	public Boolean getShowStats() {
		return showStats;
	}

	public void setShowStats(Boolean showStats) {
		this.showStats = showStats;
	}

	@Column(name = "show_recent_observations", columnDefinition = "boolean default true")
	public Boolean getShowRecentObservation() {
		return showRecentObservation;
	}

	public void setShowRecentObservation(Boolean showRecentObservation) {
		this.showRecentObservation = showRecentObservation;
	}

	@Column(name = "show_grid_map", columnDefinition = "boolean default true")
	public Boolean getShowGridMap() {
		return showGridMap;
	}

	public void setShowGridMap(Boolean showGridMap) {
		this.showGridMap = showGridMap;
	}

	@Column(name = "show_partners", columnDefinition = "boolean default true")
	public Boolean getShowPartners() {
		return showPartners;
	}

	public void setShowPartners(Boolean showPartners) {
		this.showPartners = showPartners;
	}

	@Column(name = "show_sponsors", columnDefinition = "boolean default true")
	public Boolean getShowSponsors() {
		return showSponsors;
	}

	public void setShowSponsors(Boolean showSponsors) {
		this.showSponsors = showSponsors;
	}

	@Column(name = "show_donors", columnDefinition = "boolean default true")
	public Boolean getShowDonors() {
		return showDonors;
	}

	public void setShowDonors(Boolean showDonors) {
		this.showDonors = showDonors;
	}

	@Column(name = "show_desc", columnDefinition = "boolean default true")
	public Boolean getShowDesc() {
		return showDesc;
	}

	public void setShowDesc(Boolean showDesc) {
		this.showDesc = showDesc;
	}

	@Transient
	public HomePageStats getStats() {
		return stats;
	}

	public void setStats(HomePageStats stats) {
		this.stats = stats;
	}

	@Transient
	public List<GallerySlider> getGallerySlider() {
		return gallerySlider;
	}

	public void setGallerySlider(List<GallerySlider> gallerySlider) {
		this.gallerySlider = gallerySlider;
	}

	@Transient
	public List<GalleryConfig> getMiniGallery() {
		return miniGallery;
	}

	public void setMiniGallery(List<GalleryConfig> miniGallery) {
		this.miniGallery = miniGallery;
	}

	@Transient
	public List<Map<String, Map<Long, List<MiniGallerySlider>>>> getMiniGallerySlider() {
		return miniGallerySlider;
	}

	public void setMiniGallerySlider(List<Map<String, Map<Long, List<MiniGallerySlider>>>> miniGallerySlider) {
		this.miniGallerySlider = miniGallerySlider;
	}

	public String getUgDescription() {
		return ugDescription;
	}

	public void setUgDescription(String ugDescription) {
		this.ugDescription = ugDescription;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "site_logo")
	public String getSiteLogo() {
		return siteLogo;
	}

	public void setSiteLogo(String siteLogo) {
		this.siteLogo = siteLogo;
	}

	@Column(name = "fav_icon")
	public String getFavIcon() {
		return favIcon;
	}

	public void setFavIcon(String favIcon) {
		this.favIcon = favIcon;
	}

	@Column(name = "language_id")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	@Transient
	public List<Translation> getTranslations() {
		return translations;
	}

	public void setTranslations(List<Translation> translations) {
		this.translations = translations;
	}

}
