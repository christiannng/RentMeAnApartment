package com.prog39599.beans;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Size(min=3, max=255, message="Username should be between 3 and 255")
	private String username;
	
	@Size(min=3, max=255, message="Password should be between 3 and 255")
	private String password;
	
	private boolean admin;
	
	@Transient
	private String retypepassword;
	

}
