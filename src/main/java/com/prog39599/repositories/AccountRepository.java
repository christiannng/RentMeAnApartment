package com.prog39599.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prog39599.beans.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findByUsername(String username);
	List<Account> findByUsernameAndPassword(String username, String password);
	public void deleteById(Long id);
}
