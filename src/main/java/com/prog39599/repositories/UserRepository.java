package com.prog39599.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog39599.beans.Account;
import com.prog39599.beans.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	public List<User> findAllByOrderByIdAsc();
	public void deleteById(Long id);
	public Optional<User> findById(Long id);
	public User findByAccount(Account account);
}
