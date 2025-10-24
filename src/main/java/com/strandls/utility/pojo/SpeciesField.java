package com.strandls.utility.pojo;

import java.util.List;
import java.util.ArrayList;

public class SpeciesField {
    private String name;
    private List<SpeciesField> childField;
    private List<String> values;

    // Constructors
    public SpeciesField() {
        super();
    }

    // Getters and Setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SpeciesField> getChildField() { return childField; }
    public void setChildField(List<SpeciesField> childField) { this.childField = childField; }

    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
}