/**
 * 
 */
package com.strandls.utility.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */

@Entity
@Table(name = "tags")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tags implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3031129954253359964L;
	private Long id;
	private String name;

	/**
	 * 
	 */
	public Tags() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public Tags(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
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

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
