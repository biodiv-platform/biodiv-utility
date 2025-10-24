package com.strandls.utility.pojo;

public class FieldData {
    private Long id;
    private String description;
    private Long fieldId;
    private Long speciesId;
    private String status;

    // Constructors
    public FieldData() {}

    public FieldData(Long id, String description, Long fieldId, Long speciesId, String status) {
        this.id = id;
        this.description = description;
        this.fieldId = fieldId;
        this.speciesId = speciesId;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }

    public Long getSpeciesId() { return speciesId; }
    public void setSpeciesId(Long speciesId) { this.speciesId = speciesId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}