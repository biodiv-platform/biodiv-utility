package com.strandls.utility.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedName {
	private Boolean parsed;
	private Integer quality;
	private List<QualityWarning> qualityWarnings;
	private String nomenclaturalCode;
	private String verbatim;
	private String normalized;
	private CanonicalName canonical;
	private Integer cardinality;
	private AuthorshipInfo authorship;
	private String bacteria;
	private Boolean virus;
	private String hybrid;
	private String surrogate;
	private String tail;
	private Object details;
	private List<Word> words;
	private String id;
	private String parserVersion;

	// Getters and setters
	public Boolean getParsed() {
		return parsed;
	}

	public void setParsed(Boolean parsed) {
		this.parsed = parsed;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	public List<QualityWarning> getQualityWarnings() {
		return qualityWarnings;
	}

	public void setQualityWarnings(List<QualityWarning> qualityWarnings) {
		this.qualityWarnings = qualityWarnings;
	}

	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}

	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
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

	public CanonicalName getCanonical() {
		return canonical;
	}

	public void setCanonical(CanonicalName canonical) {
		this.canonical = canonical;
	}

	public Integer getCardinality() {
		return cardinality;
	}

	public void setCardinality(Integer cardinality) {
		this.cardinality = cardinality;
	}

	public AuthorshipInfo getAuthorship() {
		return authorship;
	}

	public void setAuthorship(AuthorshipInfo authorship) {
		this.authorship = authorship;
	}

	public String getBacteria() {
		return bacteria;
	}

	public void setBacteria(String bacteria) {
		this.bacteria = bacteria;
	}

	public Boolean getVirus() {
		return virus;
	}

	public void setVirus(Boolean virus) {
		this.virus = virus;
	}

	public String getHybrid() {
		return hybrid;
	}

	public void setHybrid(String hybrid) {
		this.hybrid = hybrid;
	}

	public String getSurrogate() {
		return surrogate;
	}

	public void setSurrogate(String surrogate) {
		this.surrogate = surrogate;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}

	public Object getDetails() {
		return details;
	}

	public void setDetails(Object details) {
		this.details = details;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
				+ normalized + ", canonical=" + canonical + ", authorship=" + authorship + ", cardinality="
				+ cardinality + ", id=" + id + ", details=" + details + ", words=" + words
				+ ", surrogate=" + surrogate + ", virus=" + virus + ", hybrid=" + hybrid + ", bacteria=" + bacteria
				+ ", parserVersion=" + parserVersion + "]";
	}
}

class CanonicalName {
	private String stemmed;
	private String simple;
	private String full;

	public String getStemmed() {
		return stemmed;
	}

	public void setStemmed(String stemmed) {
		this.stemmed = stemmed;
	}

	public String getSimple() {
		return simple;
	}

	public void setSimple(String simple) {
		this.simple = simple;
	}

	public String getFull() {
		return full;
	}

	public void setFull(String full) {
		this.full = full;
	}

	@Override
	public String toString() {
		return "CanonicalName [stemmed=" + stemmed + ", simple=" + simple + ", full=" + full + "]";
	}
}

class QualityWarning {
	private String warning;
	private Integer quality;

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		return "QualityWarning [warning=" + warning + ", quality=" + quality + "]";
	}
}

class AuthorshipInfo {
	private String verbatim;
	private String normalized;
	private String year;
	private List<String> authors;
	private AuthorGroup original;
	private AuthorGroup combination;
	private AuthorGroup originalAuth;

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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public AuthorGroup getOriginal() {
		return original;
	}

	public void setOriginal(AuthorGroup original) {
		this.original = original;
	}

	public AuthorGroup getCombination() {
		return combination;
	}

	public void setCombination(AuthorGroup combination) {
		this.combination = combination;
	}

	public AuthorGroup getOriginalAuth() {
		return originalAuth;
	}

	public void setOriginalAuth(AuthorGroup originalAuth) {
		this.originalAuth = originalAuth;
	}

	@Override
	public String toString() {
		return "AuthorshipInfo [verbatim=" + verbatim + ", normalized=" + normalized + ", year=" + year + ", authors=" + authors + "]";
	}
}

class AuthorGroup {
	private List<String> authors;
	private YearInfo year;
	private Authors exAuthors;
	private Authors emendAuthors;

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public YearInfo getYear() {
		return year;
	}

	public void setYear(YearInfo year) {
		this.year = year;
	}

	public Authors getExAuthors() {
		return exAuthors;
	}

	public void setExAuthors(Authors exAuthors) {
		this.exAuthors = exAuthors;
	}

	public Authors getEmendAuthors() {
		return emendAuthors;
	}

	public void setEmendAuthors(Authors emendAuthors) {
		this.emendAuthors = emendAuthors;
	}

	@Override
	public String toString() {
		return "AuthorGroup [authors=" + authors + ", year=" + year + "]";
	}
}

class YearInfo {
	private String value;
	private Boolean isApproximate;
	private String year;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getIsApproximate() {
		return isApproximate;
	}

	public void setIsApproximate(Boolean isApproximate) {
		this.isApproximate = isApproximate;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Override
	public String toString() {
		return "YearInfo [value=" + value + ", isApproximate=" + isApproximate + ", year=" + year + "]";
	}
}

class Authors {
	private List<String> authors;
	private YearInfo year;

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public YearInfo getYear() {
		return year;
	}

	public void setYear(YearInfo year) {
		this.year = year;
	}

	@Override
	public String toString() {
		return "Authors [authors=" + authors + ", year=" + year + "]";
	}
}

class Word {
	private String verbatim;
	private String normalized;
	private String wordType;
	private Integer start;
	private Integer end;

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

	public String getWordType() {
		return wordType;
	}

	public void setWordType(String wordType) {
		this.wordType = wordType;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "Word [verbatim=" + verbatim + ", normalized=" + normalized + ", wordType=" + wordType + ", start=" + start + ", end=" + end + "]";
	}
}