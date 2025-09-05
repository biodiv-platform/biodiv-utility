/**
 * 
 */
package com.strandls.utility.pojo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedName {

	private Boolean parsed;
	private Float quality;
	private String verbatim;
	private String normalized;
	private CanonicalName CanonicalName;
	private String authorship;
	List<Object> details = new ArrayList<>();
	List<Object> positions = new ArrayList<>();
	private boolean surrogate;
	private boolean virus;
	private boolean hybrid;
	private boolean bacteria;
	private String nameStringId;
	private String parserVersion;

	public Boolean getParsed() {
		return parsed;
	}

	public void setParsed(Boolean parsed) {
		this.parsed = parsed;
	}

	public Float getQuality() {
		return quality;
	}

	public void setQuality(Float quality) {
		this.quality = quality;
	}

	public String getVerbatim() {
		return verbatim;
	}

	public void setVerbatim(String verbatim) {
		this.verbatim = verbatim;
	}

	public String getNormalized() {
		return normalized;
	}

	public void setNormalized(String normalized) {
		this.normalized = normalized;
	}

	public CanonicalName getCanonicalName() {
		return CanonicalName;
	}

	public void setCanonicalName(CanonicalName canonicalName) {
		CanonicalName = canonicalName;
	}

	public String getAuthorship() {
		return authorship;
	}

	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	public List<Object> getDetails() {
		return details;
	}

	public void setDetails(List<Object> details) {
		this.details = details;
	}

	public List<Object> getPositions() {
		return positions;
	}

	public void setPositions(List<Object> positions) {
		this.positions = positions;
	}

	public boolean isSurrogate() {
		return surrogate;
	}

	public void setSurrogate(boolean surrogate) {
		this.surrogate = surrogate;
	}

	public boolean isVirus() {
		return virus;
	}

	public void setVirus(boolean virus) {
		this.virus = virus;
	}

	public boolean isHybrid() {
		return hybrid;
	}

	public void setHybrid(boolean hybrid) {
		this.hybrid = hybrid;
	}

	public boolean isBacteria() {
		return bacteria;
	}

	public void setBacteria(boolean bacteria) {
		this.bacteria = bacteria;
	}

	public String getNameStringId() {
		return nameStringId;
	}

	public void setNameStringId(String nameStringId) {
		this.nameStringId = nameStringId;
	}

	public String getParserVersion() {
		return parserVersion;
	}

	public void setParserVersion(String parserVersion) {
		this.parserVersion = parserVersion;
	}

	@Override
	public String toString() {
		return "ParsedName [parsed=" + parsed + ", quality=" + quality + ", verbatim=" + verbatim + ", normalized="
				+ normalized + ", CanonicalName=" + CanonicalName + ", authorship=" + authorship + ", details="
				+ details + ", positions=" + positions + ", surrogate=" + surrogate + ", virus=" + virus + ", hybrid="
				+ hybrid + ", bacteria=" + bacteria + ", nameStringId=" + nameStringId + ", parserVersion="
				+ parserVersion + "]";
	}

}

class CanonicalName {
	private String full;
	private String simple;
	private String stem;

	// Getter Methods

	public String getFull() {
		return full;
	}

	public String getSimple() {
		return simple;
	}

	public String getStem() {
		return stem;
	}

	// Setter Methods

	public void setFull(String full) {
		this.full = full;
	}

	public void setSimple(String simple) {
		this.simple = simple;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}
}
