/**
 * 
 */
package com.strandls.utility.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */

@Entity
@Table(name = "flag")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flag implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5614378769650481194L;

	private Long id;
	private Long authorId;
	private Date createdOn;
	private String flag;
	private String notes;
	private Long objectId;
	private String objectType;

	/**
	 * 
	 */
	public Flag() {
		super();
	}

	/**
	 * @param id
	 * @param authorId
	 * @param createdOn
	 * @param flag
	 * @param notes
	 * @param objectId
	 * @param objectType
	 */
	public Flag(Long id, Long authorId, Date createdOn, String flag, String notes, Long objectId, String objectType) {
		super();
		this.id = id;
		this.authorId = authorId;
		this.createdOn = createdOn;
		this.flag = flag;
		this.notes = notes;
		this.objectId = objectId;
		this.objectType = objectType;
	}

	@Id
	@GeneratedValue
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "author_id")
	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	@Column(name = "created_on")
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Column(name = "flag")
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	@Column(name = "notes")
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Column(name = "object_id")
	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	@Column(name = "object_type")
	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

}
