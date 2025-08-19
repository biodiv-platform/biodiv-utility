/** */
package com.strandls.utility.pojo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Abhishek Rudra
 */
@Entity
@Table(name = "habitat")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Habitat implements Serializable {

	/** */
	private static final long serialVersionUID = -175751465352099324L;

	private Long id;
	private Integer habitatOrder;
	private String name;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "habitat_order")
	public Integer getHabitatOrder() {
		return habitatOrder;
	}

	public void setHabitatOrder(Integer habitatOrder) {
		this.habitatOrder = habitatOrder;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
