package com.prog39599.beans;



import java.time.LocalDate;
//import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
//import javax.persistence.Temporal;
//import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
	
	@NotNull
	private Double rent;
	
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
	
	private boolean approved;
	
	//@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(columnDefinition = "DATE")
	private LocalDate rentFrom;
	
	//@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(columnDefinition = "DATE")
	private LocalDate   rentTo;
	
	public String getAddress() {
		return street + ", " + city + ", " + province + ", " + postalCode;
	}
}
