package com.strandls.utility.pojo;

public class HomePageStats {

	private Long species;
	private Long observation;
	private Long maps;
	private Long documents;
	private Long activeUser;

	/**
	 * 
	 */
	public HomePageStats() {
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
	public HomePageStats(Long species, Long observation, Long maps, Long documents, Long activeUser) {
		super();
		this.species = species;
		this.observation = observation;
		this.maps = maps;
		this.documents = documents;
		this.activeUser = activeUser;
	}

	public Long getSpecies() {
		return species;
	}

	public void setSpecies(Long species) {
		this.species = species;
	}

	public Long getObservation() {
		return observation;
	}

	public void setObservation(Long observation) {
		this.observation = observation;
	}

	public Long getMaps() {
		return maps;
	}

	public void setMaps(Long maps) {
		this.maps = maps;
	}

	public Long getDocuments() {
		return documents;
	}

	public void setDocuments(Long documents) {
		this.documents = documents;
	}

	public Long getActiveUser() {
		return activeUser;
	}

	public void setActiveUser(Long activeUser) {
		this.activeUser = activeUser;
	}

}
