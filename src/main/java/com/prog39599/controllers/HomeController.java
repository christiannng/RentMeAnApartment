package com.prog39599.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.prog39599.beans.Account;
import com.prog39599.beans.Apartment;
import com.prog39599.beans.User;
import com.prog39599.repositories.AccountRepository;
import com.prog39599.repositories.ApartmentRepository;
import com.prog39599.repositories.UserRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class HomeController {

	private UserRepository userRepo;
	private AccountRepository accountRepo;
	private ApartmentRepository apartmentRepo;
	private static User currentUser;
	private static Apartment currentApt;

	
	@GetMapping("/")
	public String index(Model model, @ModelAttribute User user) {
		currentUser = null;
		
		return "index";
	}
	
	/*
	@GetMapping("/")
	public String index(Model model, @ModelAttribute Apartment apt) {
		
		model.addAttribute("apartmentList", apartmentRepo.findAll());
		return "adminApartmentDB";
		
	}*/

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
		model.addAttribute("aptList", apartmentRepo.findAll());
		return "browse";
	}
	@RequestMapping("/rentProcess")
	public String rentApt(Model model, @RequestParam String myStr) {
		Optional<Apartment> aptSelected = apartmentRepo.findById(Long.parseLong(myStr));
		currentApt = aptSelected.get();
		
		if(true) System.out.println("");//will use an if here if we decide to list all then check availablility 
										//otherwise will remove it if we list only available in browse
		if(currentUser == null) {
			currentUser = User.builder().firstname("Guest").build();
			
		}
		model.addAttribute("apt", currentApt);
		model.addAttribute("user", currentUser);
	
	return "rent";
	}
	@GetMapping("/receipt")
	public String receipt(Model model) {

	
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
		model.addAttribute("aptList", apartmentRepo.findAll());
		return "browse";
	}

	@GetMapping("/admin/database/user")
	public String queryAccountDbError(Model model, @ModelAttribute User user) {
		return "index";
	}
	
	@PostMapping("/admin/database/user")
	public String queryAccountDb(Model model, @ModelAttribute User user) {
		if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			List<User> users = userRepo.findAllByOrderByIdAsc();
			model.addAttribute("userList", users);
			
			return "adminUserDB";
		}
		
		return "index";
	}
	
	@GetMapping("/admin/database/user/add")
	public String addUserAdminHandleError(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "index";
	}
	
	@PostMapping("/admin/database/user/add")
	public String addUserAdmin(Model model, @ModelAttribute User user,
			@RequestParam String newUsername, 
			@RequestParam String newPassword, 
			@RequestParam(defaultValue="false") boolean newAdmin) {
		
		if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			
			Account accountToAdd = Account.builder().username(newUsername).password(newPassword).admin(newAdmin).build();
			user.setAccount(accountToAdd);
			if(!isValidated(user)) {
				model.addAttribute("statusBad", "Cannot add new user due to validation failure!");
				List<User> users = userRepo.findAllByOrderByIdAsc();
				model.addAttribute("userList", users);
				
				return "adminUserDB";
			}
			
			user.getAccount().setRetypepassword(user.getAccount().getPassword());
			user.setId(null);
			userRepo.save(user);
			accountRepo.save(user.getAccount());
			
			model.addAttribute("statusGood", "Added User Successful!!!");
			List<User> users = userRepo.findAllByOrderByIdAsc();
			model.addAttribute("userList", users);
			
			return "adminUserDB";
		}
		
		return "index";
	}
	
	@PostMapping("/admin/database/user/update")
	public String updateUser(Model model, @ModelAttribute User user, 
			@RequestParam String updatedUsername, 
			@RequestParam String updatedPassword, 
			@RequestParam(defaultValue="false") boolean updatedAdmin) {
		if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			Account account = Account.builder().username(updatedUsername).password(updatedPassword).admin(updatedAdmin).build();
			user.setAccount(account);
			if(updateUser(user)) {
				model.addAttribute("statusGood", "Updated User!!!");
			} else {
				model.addAttribute("statusBad", "There is nothing to update!!!");
			}
			
			List<User> users = userRepo.findAllByOrderByIdAsc();
			model.addAttribute("userList", users);
		
			return "adminUserDB";
		}
		
		return "index";
	}
	
	@PostMapping("/admin/database/user/delete")
	public String deleteUser(Model model, @RequestParam Long movieId) {
		if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			
			Optional<User> userInDB = userRepo.findById(movieId);
			User userToDelete = userInDB.get();
			
			accountRepo.deleteById(userToDelete.getAccount().getId());
			userRepo.deleteById(movieId);
			
			
			List<User> users = userRepo.findAllByOrderByIdAsc();
			model.addAttribute("userList", users);
			model.addAttribute("statusGood", "Deleted The User Successfully!!!");
		
			return "adminUserDB";
		}
		
		return "index";
	}
	
	private boolean isValidated(User user) {
		if(user.getFirstname().isBlank()) return false; 
		if(user.getLastname().isBlank()) return false;
		if(!user.getEmail().contains("@")) return false;
		if(user.getAccount().getUsername().length() < 3) return false;
		if(user.getAccount().getPassword().length() < 3) return false;
		
		return true;
	}
	
	private boolean updateUser(User user) {
		
		boolean isAnythingToUpdate = false;
		Optional<User> userInDB = userRepo.findById(user.getId());
		User foundUser = userInDB.get();
		
		if(!user.getFirstname().isBlank() && !foundUser.getFirstname().equals(user.getFirstname())) {
			foundUser.setFirstname(user.getFirstname());
			isAnythingToUpdate = true;
		}
		
		if(!user.getLastname().isBlank() && !user.getLastname().equals(foundUser.getLastname())) {
			foundUser.setLastname(user.getLastname());
			isAnythingToUpdate = true;
		}
		
		if(user.getEmail().contains("@") && !user.getEmail().equals(foundUser.getEmail())) {
			foundUser.setEmail(user.getEmail());
			isAnythingToUpdate = true;
		}
		
		Account foundAccount = foundUser.getAccount();
		Account account = user.getAccount();
		if(account.getUsername().length() >= 3 && !account.getUsername().equals(foundAccount.getUsername())) {
			foundAccount.setUsername(account.getUsername());
			isAnythingToUpdate = true;
		}
		
		if(account.getPassword().length() >= 3 && !account.getPassword().equals(foundAccount.getPassword())) {
			foundAccount.setPassword(account.getPassword());
			isAnythingToUpdate = true;
		}
		
		if(account.isAdmin() != foundAccount.isAdmin()) {
			foundAccount.setAdmin(account.isAdmin());
			isAnythingToUpdate = true;
		}
		
		if(isAnythingToUpdate) {
			accountRepo.save(foundAccount);
			userRepo.save(foundUser);
		}
		
		return isAnythingToUpdate;
	}

	@GetMapping("/admin/database/apartment")
	public String queryMovieDbError(Model model, @ModelAttribute Apartment movie, @ModelAttribute User user) {
		return "index";
	}
	
	@PostMapping("/admin/database/apartment")
	public String queryMovieDb(Model model, @ModelAttribute Apartment apt) {
		
		if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			model.addAttribute("movieList", apartmentRepo.findAll());
			return "adminMovieDB";
		}
		
		return "index";
	}
	
	@PostMapping("/admin/database/apartment/add")
	public String addApt(Model model, @ModelAttribute Apartment apt) {
		
		boolean a = isAptValidated(apt);
		if(!isAptValidated(apt)) {
			model.addAttribute("statusBad", "Could not add apartment due to validation error");
			return "adminApartmentDB";
		} else {
			apartmentRepo.save(apt);
			model.addAttribute("statusGood", "Added Apartment Successfully");
			model.addAttribute("apartmentList", apartmentRepo.findAll());
			return "adminApartmentDB";
		}
			
		
		/*if(currentUser == null) {
			return "index";
		}
		
		if(currentUser.getAccount().isAdmin()) {
			if(isAptValidated(apt))
			model.addAttribute("movieList", apartmentRepo.findAll());
			return "adminMovieDB";
		}
		
		return "index"; */
		
	}
	
	@PostMapping("/admin/database/apartment/update")
	public String updateApt(Model model, @ModelAttribute Apartment apt, 
			@RequestParam(defaultValue="false") boolean isAvailableNow, @RequestParam String province) {
		
		apt.setStatus(isAvailableNow);
		apt.setProvince(province);
		
		if(updateApartment(apt)) {
			model.addAttribute("statusGood", "Updated Apartment!!!");
		} else {
			model.addAttribute("statusBad", "There is nothing to update!!!");
		}
		
		model.addAttribute("apartmentList", apartmentRepo.findAllByOrderByIdAsc());
	
		return "adminApartmentDB";
	}
	
	@PostMapping("/admin/database/apartment/delete")
	public String deleteApt(Model model, @ModelAttribute Apartment apt, @RequestParam Long apartmentId) {
		apartmentRepo.deleteById(apartmentId);
		
		model.addAttribute("statusGood", "Deleted The Apartment Successfully!!!");
		List<Apartment> apartments = apartmentRepo.findAllByOrderByIdAsc();
		model.addAttribute("apartmentList", apartments);
		
		return "adminApartmentDB";
	}
	
	private boolean updateApartment(Apartment apt) {
		
		boolean isAnythingToUpdate = false;
		Optional<Apartment> aptInDB = apartmentRepo.findById(apt.getId());
		Apartment foundApt = aptInDB.get();
		
		foundApt.setProvince(apt.getProvince());
		foundApt.setApartmentNo(apt.getApartmentNo());
		isAnythingToUpdate = true;
		
		if(!apt.getStreet().isBlank() && !foundApt.getStreet().equals(foundApt.getStreet())) {
			foundApt.setStreet(apt.getStreet());
			isAnythingToUpdate = true;
		}
		
		if(!apt.getCity().isBlank() && !foundApt.getCity().equals(foundApt.getCity())){
			foundApt.setCity(apt.getCity());
			isAnythingToUpdate = true;
		}
		
		if(!apt.getPostalCode().isBlank() && !foundApt.getPostalCode().equals(foundApt.getPostalCode())) {
			foundApt.setPostalCode(apt.getPostalCode());
			isAnythingToUpdate = true;
		}
		
		if(!apt.getPropertyManager().isBlank() && !foundApt.getPropertyManager().equals(foundApt.getPropertyManager())) {
			foundApt.setPropertyManager(apt.getPropertyManager());
			isAnythingToUpdate = true;
		}
		
		if(!foundApt.isStatus() != foundApt.isStatus()) {
			foundApt.setStatus(apt.isStatus());
			isAnythingToUpdate = true;
		}
		
		if(apt.getRentFrom() != null && foundApt.getRentFrom() != apt.getRentFrom()) {
			foundApt.setRentFrom(apt.getRentFrom());
			isAnythingToUpdate = true;
		}
		
		if(apt.getRentTo() != null && foundApt.getRentTo() != apt.getRentTo()) {
			foundApt.setRentTo(apt.getRentTo());
			isAnythingToUpdate = true;
		}
		
		if(isAnythingToUpdate) {
			apartmentRepo.save(foundApt);
		}
		
		return isAnythingToUpdate;
	}
	
	private boolean isAptValidated(Apartment apt) {
		boolean isValidated = true;
		
		if(apt.getStreet().isBlank()) isValidated = false;
		if(apt.getCity().isBlank()) isValidated = false;
		if(apt.getProvince().isBlank()) isValidated = false;
		if(apt.getPostalCode().isBlank()) isValidated = false;
		if(apt.getPropertyManager().isBlank()) isValidated = false;
		if(apt.getRentFrom() == null) isValidated = false;
		if(apt.getRentTo() == null) isValidated = false;
		
		return isValidated;
	}
}
