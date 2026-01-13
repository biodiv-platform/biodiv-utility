package com.strandls.utility.pojo;

import java.util.List;

public class SpeciesField {
	private Long id;
	private String name;
	private List<SpeciesField> childField;
	private List<FieldValue> values;
	private List<Trait> traits;

	public SpeciesField() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SpeciesField> getChildField() {
		return childField;
	}

	public void setChildField(List<SpeciesField> childField) {
		this.childField = childField;
	}

	public List<FieldValue> getValues() {
		return values;
	}

	public void setValues(List<FieldValue> values) {
		this.values = values;
	}

	public List<Trait> getTraits() {
		return traits;
	}

	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}
}