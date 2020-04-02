package com.prog39599.beans;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty(message="Last name cannot be empty")
	private String lastname;
	
	@NotEmpty(message="First name cannot be empty")
	private String firstname;

	@Email
	private String email;
	
	private int age;
	
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinTable(name="USER_ACCOUNT", 
		joinColumns = @JoinColumn(name="USER_ID"), 
		inverseJoinColumns = @JoinColumn(name="ACCOUNT_ID"))
	private Account account;
	
}