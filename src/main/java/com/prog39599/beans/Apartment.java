package com.prog39599.beans;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Apartment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty
	private String street;
	
	@NotEmpty
	private String city;
	
	private String province;
	
    //@Pattern(regexp = "[a-zA-Z][0-9][a-zA-Z][0-9][a-zA-Z][0-9]")
	private String postalCode;
	
	private int apartmentNo;
	
    @NotEmpty
	private String propertyManager;
	
	private boolean status;
	
	private String imageURL;
	
	@Column(name="Availablity") 
	private boolean available;
	
	@Column(columnDefinition = "boolean default false")
	private boolean approved;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date rentFrom;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date rentTo;
}
