package com.strandls.utility.pojo;

public class FieldValue {
    private Long id;
    private String header;
    private String description;
    private String label;
    private String path;
    private Integer displayOrder;
    private Long fieldId;
    private String fieldDescription;
    private String attributions;
    private String audienceType;
    private FieldData fieldData;
    private Object speciesFieldResource;

    // Constructors
    public FieldValue() {
        super();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }

    public String getFieldDescription() { return fieldDescription; }
    public void setFieldDescription(String fieldDescription) { this.fieldDescription = fieldDescription; }

    public String getAttributions() { return attributions; }
    public void setAttributions(String attributions) { this.attributions = attributions; }

    public String getAudienceType() { return audienceType; }
    public void setAudienceType(String audienceType) { this.audienceType = audienceType; }

    public FieldData getFieldData() { return fieldData; }
    public void setFieldData(FieldData fieldData) { this.fieldData = fieldData; }

    public Object getSpeciesFieldResource() { return speciesFieldResource; }
    public void setSpeciesFieldResource(Object speciesFieldResource) { this.speciesFieldResource = speciesFieldResource; }
}