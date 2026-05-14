package com.strandls.utility.pojo;

import java.util.List;
import java.util.Map;

public class Trait {
	private String name;
	private String dataType;
	private Map<Long, String> options;
	private List<TraitValue> values;
	private String units;
	private String icon;

	public Trait() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Map<Long, String> getOptions() {
		return options;
	}

	public void setOptions(Map<Long, String> options) {
		this.options = options;
	}

	public List<TraitValue> getValues() {
		return values;
	}

	public void setValues(List<TraitValue> values) {
		this.values = values;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}