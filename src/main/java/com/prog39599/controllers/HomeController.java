package com.prog39599.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.prog39599.beans.Account;
import com.prog39599.beans.User;
import com.prog39599.repositories.AccountRepository;
import com.prog39599.repositories.UserRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class HomeController {

	private UserRepository userRepo;
	private AccountRepository accountRepo;
	private static User currentUser;

	@GetMapping("/")
	public String index(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "index";
		
		/*
		if(currentUser == null) {
			return "index";
		} else {
			if(currentUser.getAccount().isAdmin()) {
				return "admin";
			} else {
				return "homepage";
			}
		}*/
		
		
	}

	@GetMapping("/registerNewAccount")
	public String handleErrorRegister(Model model, @ModelAttribute User user) {
		return "index";
	}
	
	@PostMapping("/registerNewAccount")
	public String newAccount(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "registration";
	}

	@GetMapping("/validateAccount")
	public String handleErrorValidateAccount(Model model, @ModelAttribute User user) {
		return "index";
	}
	
	@PostMapping("/validateAccount")
	public String validateAccount(Model model, @ModelAttribute User user) {

		boolean hasError = false;

		if (user.getFirstname() == "") {
			hasError = true;
			model.addAttribute("error_firstname", "First name cannot be empty.");
		}
		if (user.getLastname() == "") {
			hasError = true;
			model.addAttribute("error_lastname", "Last name cannot be empty.");
		}
		if (!user.getEmail().contains("@")) {
			hasError = true;
			model.addAttribute("error_email", "Not a valid email.");
		}
		if (user.getAccount().getUsername() == "") {
			hasError = true;
			model.addAttribute("error_username", "Username must have at least 6 characters.");
		} else {
			if (!accountRepo.findByUsername(user.getAccount().getUsername()).isEmpty()) {
				hasError = true;
				model.addAttribute("error_username", "Username is already taken.");

				return "registration";
			}
		}

		if (user.getAccount().getPassword().length() < 6) {
			hasError = true;
			model.addAttribute("error_password", "Password must have at least 6 characters.");
		} else {
			if (!user.getAccount().getRetypepassword().toString().equals(user.getAccount().getPassword().toString())) {
				hasError = true;
				model.addAttribute("error_retypepassword", "Password must be matched above.");
			}
		}

		if (hasError) {
			return "registration";
		} else {
			accountRepo.save(user.getAccount());
			userRepo.save(user);
			model.addAttribute("statusGood", "Registration Successful");
			return "index";
		}
	}

	@GetMapping("/continueAsGuest")
	public String countinueAsGuets2(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "homepage";
	}
	
	@PostMapping("/continueAsGuest")
	public String countinueAsGuets(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "homepage";
	}

	@GetMapping("/login")
	public String handleErrorTryToLogin(Model model, @ModelAttribute User user) {
		return "index";
	}
	
	@PostMapping("/login")
	public String tryToLogin(Model model, @ModelAttribute User user) {
		Account currentAccount = user.getAccount();
		
		currentUser = null;
		if (currentAccount == null) {
			return "index";
		}

		
		List<Account> accountFoundInDB = accountRepo.findByUsernameAndPassword(currentAccount.getUsername(),
				currentAccount.getPassword());
		
		User userFoundInDB;
		if (accountFoundInDB.size() != 1) {
			model.addAttribute("status", "Login Failed");
			return "index";
		} else {
			userFoundInDB = userRepo.findByAccount(accountFoundInDB.get(0));
			if(userFoundInDB == null) {
				model.addAttribute("status", "Login Failed");
				return "index";
			}
		}
		
		currentUser = userFoundInDB;
		currentUser.setAccount(accountFoundInDB.get(0));
		if (accountFoundInDB.get(0).isAdmin()) {
			return "admin";
		}

		return "homepage";
	}
	
	
}
