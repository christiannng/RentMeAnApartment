package com.prog39599.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog39599.beans.Apartment;
//import com.prog39599.beans.User;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
	public List<Apartment> findAllByOrderByIdAsc();
	
	public List<Apartment> findByStatusIsTrue();
	
	public List<Apartment> findByApprovedIsTrue();
	
	public List<Apartment> findByApprovedIsTrueOrderByStatus();
}
