package com.prog39599.controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
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
	private static String guestEmail;
	
	@Autowired
    private JavaMailSender javaMailSender;
	
	@GetMapping("/")
	public String index(Model model, @ModelAttribute User user) {
		currentUser = null;
		//sendEmail("rentertest101@gmail.com", "Confirmaiton Email", "THIS IS TOTAL BS");
		
		return "index";
	}

	/*
	 * @GetMapping("/") public String index(Model model, @ModelAttribute Apartment
	 * apt) {
	 * 
	 * model.addAttribute("apartmentList", apartmentRepo.findAll()); return
	 * "adminApartmentDB";
	 * 
	 * }
	 */

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
			user.getAccount().setPassword(encryptWithMD5(user.getAccount().getPassword()));;
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
	
	@GetMapping("/home")
	public String homepage(Model model) {
		model.addAttribute("user", currentUser); //added this for users to back out from confirm rent page
		return "homepage";
	}
	@PostMapping("/continueAsGuest")
	public String countinueAsGuets(Model model, @ModelAttribute User user) {
		currentUser = null;
		//model.addAttribute("aptList", apartmentRepo.findByStatusIsTrue());
		model.addAttribute("aptList", apartmentRepo.findByApprovedIsTrueOrderByStatus());
		return "browse";
	}
	/*
	@RequestMapping("/guestEmail")
	public String guestEmail(Model model, @RequestParam String email) {
		//might need to check for more things
		if(email.isBlank()) {
			model.addAttribute("error_email", "The email cannot be empty!");
			model.addAttribute("user", currentUser);
			model.addAttribute("apt", currentApt);
			return "guestRent";
		}
		currentUser.setEmail(email);
		currentUser = userRepo.save(currentUser);
		model.addAttribute("apt", currentApt);
		model.addAttribute("user", currentUser);
		return "rent";
	}
	*/
	
	@RequestMapping("/user/proceedRent/guestEmail")
	public String guestEmail(Model model, @RequestParam String guestEmail) {
		HomeController.guestEmail = guestEmail;
		model.addAttribute("apt", currentApt);
		
		if (currentUser == null) model.addAttribute("name", "Guest");
		else model.addAttribute("name", currentUser.getFirstname());
		return "rent";
	}
	
	@RequestMapping("/user/proceedRent")
	public String proceedRent(Model model, @RequestParam String aptId) {
		Optional<Apartment> aptSelected = apartmentRepo.findById(Long.parseLong(aptId));
		currentApt = aptSelected.get();
		
		if (currentUser == null) {
			model.addAttribute("apt", currentApt);
			return "guestRent";
		}
		
		model.addAttribute("apt", currentApt);
		model.addAttribute("user", currentUser);
		model.addAttribute("name", currentUser.getFirstname());
		return "rent";
	}
	
	@RequestMapping("/user/proceedRent/confirm")
	public String confirm(Model model) {
		
		//making apt unavailable, we need to decide what
		currentApt.setStatus(true);
		currentApt = apartmentRepo.save(currentApt);
		
		String email, name;
		if(currentUser != null) {
			email = currentUser.getEmail();
			name = currentUser.getFirstname();
		}
		else {
			email = guestEmail;
			name = "My Dear Friend!";
		}
	
		System.out.println("email is " + email);
		
		sendEmail(email, "Confirmaiton Email For Your New Awesome Apartment", summaryApt(name));
		
		model.addAttribute("email", email);
		return "receipt";
	}
	
	/*
	@RequestMapping("/rentProcess")
	public String rentApt(Model model, @RequestParam String myStr) {
		Optional<Apartment> aptSelected = apartmentRepo.findById(Long.parseLong(myStr));
		currentApt = aptSelected.get();
		
		if (true)
			System.out.println("");// will use an if here if we decide to list all then check availablility
									// otherwise will remove it if we list only available in browse
		if (currentUser == null) {
			currentUser = User.builder().firstname("Guest").lastname(" ").build();
			currentUser = userRepo.save(currentUser);
			model.addAttribute("user", currentUser);
			model.addAttribute("apt", currentApt);
			return "guestRent";
		}
		model.addAttribute("apt", currentApt);
		model.addAttribute("user", currentUser);

		return "rent";
	}
	*/

	/*
	@GetMapping("/receipt")
	public String receipt(Model model, @RequestParam String myStr, @RequestParam String userId) {
		Optional<Apartment> aptSelected = apartmentRepo.findById(Long.parseLong(myStr));
		currentApt = aptSelected.get(); //do i need to do this, pls remind to ask u
		
		Optional<User> userSelected = userRepo.findById(Long.parseLong(userId));
		currentUser = userSelected.get(); //do i need to do this, pls remind to ask u
		
		String email, aptBooked;
		//making apt unavailable, we need to decide what
		currentApt.setAvailable(false);
		currentApt.setStatus(false);
		currentApt = apartmentRepo.save(currentApt);
		email = currentUser.getEmail();
		System.out.println("email is " + email);
		aptBooked = "Hello " + currentUser.getFirstname() + ",\n\n" +
					"You have booked this Apartment:\n" + 
					"Apartment No: " +currentApt.getApartmentNo()+
					"\nStreet: " +currentApt.getStreet()+
					", City: " +currentApt.getCity()+
					", Province: " +currentApt.getProvince()+
					", Postal Code: " +currentApt.getPostalCode()+
					"\n\n The rent will be: " +currentApt.getRent()+
					"\n\n Your property manager will be: " +currentApt.getPropertyManager()+
					"\n\n The period for the rent is from: " +currentApt.getRentFrom()+
					" to " +currentApt.getRentTo()+
					"\n\n Here is an image of the apartment:\n" + currentApt.getImageURL()+
					"\n\nRegards,\n"+currentApt.getPropertyManager();
					
		
		
		sendEmail(email, "<Confirmaiton Email>",aptBooked);
		model.addAttribute("user", currentUser);
		return "receipt";
	}
*/
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
				encryptWithMD5(currentAccount.getPassword()));

		User userFoundInDB;
		if (accountFoundInDB.size() != 1) {
			model.addAttribute("status", "Login Failed");
			return "index";
		} else {
			userFoundInDB = userRepo.findByAccount(accountFoundInDB.get(0));
			if (userFoundInDB == null) {
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

		/*
		 * model.addAttribute("aptList", apartmentRepo.findAll()); return "browse";
		 */
	}
	
	@GetMapping("/adminPage")
	public String adminPage() {
		
		return "admin"; //not sure if we need to pass anything here, remind me and delete comment
	}
	
	
	@GetMapping("/user/browse")
	public String getBrowse(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "index";
	}

	@PostMapping("/user/browse")
	public String postBrowse(Model model) {
		//model.addAttribute("aptList", apartmentRepo.findByStatusIsTrue());
		
		model.addAttribute("aptList", apartmentRepo.findByApprovedIsTrueOrderByStatus());
		return "browse";
	}

	@GetMapping("/user/listAnApartment")
	public String getListApt(Model model, @ModelAttribute User user) {
		currentUser = null;
		return "index";
	}

	@PostMapping("/user/listAnApartment")
	public String postListApt(Model model, @ModelAttribute Apartment apartment) {
		if (currentUser == null) {
			return "index";
		} else {
			return "userListApt";
		}
	}

	@GetMapping("/user/database/apartment/add")
	public String getUserAddApt(Model model, @ModelAttribute User user) {
		return "index";
	}

	@PostMapping("/user/database/apartment/add")
	public String postUserAddApt(Model model, @ModelAttribute Apartment apt) {

		if (!isAptValidated(apt)) {
			model.addAttribute("statusBad", "Could not add apartment due to validation error");
			return "userListApt";
		} else {
			apartmentRepo.save(apt);
			model.addAttribute("statusGood", "Added Apartment Successfully");
			return "homepage";
		}
	}

	@GetMapping("/admin/database/user")
	public String queryAccountDbError(Model model, @ModelAttribute User user) {
		return "index";
	}

	@PostMapping("/admin/database/user")
	public String queryAccountDb(Model model, @ModelAttribute User user) {
		if (currentUser == null) {
			return "index";
		}

		if (currentUser.getAccount().isAdmin()) {
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
	public String addUserAdmin(Model model, @ModelAttribute User user, @RequestParam String newUsername,
			@RequestParam String newPassword, @RequestParam(defaultValue = "false") boolean newAdmin) {

		if (currentUser == null) {
			return "index";
		}

		if (currentUser.getAccount().isAdmin()) {

			Account accountToAdd = Account.builder().username(newUsername).password(newPassword).admin(newAdmin)
					.build();
			user.setAccount(accountToAdd);
			if (!isValidated(user)) {
				model.addAttribute("statusBad", "Cannot add new user due to validation failure!");
				List<User> users = userRepo.findAllByOrderByIdAsc();
				model.addAttribute("userList", users);

				return "adminUserDB";
			}

			user.getAccount().setRetypepassword(user.getAccount().getPassword());
			user.getAccount().setPassword(encryptWithMD5(user.getAccount().getPassword()));
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
	public String updateUser(Model model, @ModelAttribute User user, @RequestParam String updatedUsername,
			@RequestParam String updatedPassword, @RequestParam(defaultValue = "false") boolean updatedAdmin) {
		if (currentUser == null) {
			return "index";
		}

		if (currentUser.getAccount().isAdmin()) {
			Account account = Account.builder().username(updatedUsername).password(updatedPassword).admin(updatedAdmin)
					.build();
			user.setAccount(account);
			if (updateUser(user)) {
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
		if (currentUser == null) {
			return "index";
		}

		if (currentUser.getAccount().isAdmin()) {

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
		if (user.getFirstname().isBlank())
			return false;
		if (user.getLastname().isBlank())
			return false;
		if (!user.getEmail().contains("@"))
			return false;
		if (user.getAccount().getUsername().length() < 3)
			return false;
		if (user.getAccount().getPassword().length() < 3)
			return false;

		return true;
	}

	private boolean updateUser(User user) {

		boolean isAnythingToUpdate = false;
		Optional<User> userInDB = userRepo.findById(user.getId());
		User foundUser = userInDB.get();

		if (!user.getFirstname().isBlank() && !foundUser.getFirstname().equals(user.getFirstname())) {
			foundUser.setFirstname(user.getFirstname());
			isAnythingToUpdate = true;
		}

		if (!user.getLastname().isBlank() && !user.getLastname().equals(foundUser.getLastname())) {
			foundUser.setLastname(user.getLastname());
			isAnythingToUpdate = true;
		}

		if (user.getEmail().contains("@") && !user.getEmail().equals(foundUser.getEmail())) {
			foundUser.setEmail(user.getEmail());
			isAnythingToUpdate = true;
		}

		Account foundAccount = foundUser.getAccount();
		Account account = user.getAccount();
		if (account.getUsername().length() >= 3 && !account.getUsername().equals(foundAccount.getUsername())) {
			foundAccount.setUsername(account.getUsername());
			isAnythingToUpdate = true;
		}

		if (account.getPassword().length() >= 3 && 
				!encryptWithMD5(account.getPassword()).equals(encryptWithMD5(foundAccount.getPassword()))) {
			foundAccount.setPassword(encryptWithMD5(account.getPassword()));
			isAnythingToUpdate = true;
		}

		if (account.isAdmin() != foundAccount.isAdmin()) {
			foundAccount.setAdmin(account.isAdmin());
			isAnythingToUpdate = true;
		}

		if (isAnythingToUpdate) {
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

		if (currentUser == null) {
			return "index";
		}

		if (currentUser.getAccount().isAdmin()) {
			model.addAttribute("apartmentList", apartmentRepo.findAll());
			return "adminApartmentDB";
		}

		return "index";
	}

	@PostMapping("/admin/database/apartment/add")
	public String addApt(Model model, @ModelAttribute Apartment apt) {

		if (!isAptValidated(apt)) {
			model.addAttribute("statusBad", "Could not add apartment due to validation error");
			return "adminApartmentDB";
		} else {
			apartmentRepo.save(apt);
			model.addAttribute("statusGood", "Added Apartment Successfully");
			model.addAttribute("apartmentList", apartmentRepo.findAll());
			return "adminApartmentDB";
		}

		/*
		 * if(currentUser == null) { return "index"; }
		 * 
		 * if(currentUser.getAccount().isAdmin()) { if(isAptValidated(apt))
		 * model.addAttribute("movieList", apartmentRepo.findAll()); return
		 * "adminMovieDB"; }
		 * 
		 * return "index";
		 */

	}

	@PostMapping("/admin/database/apartment/update")
	public String updateApt(Model model, @ModelAttribute Apartment apt,
			@RequestParam(defaultValue = "false") boolean isAvailableNow, 
			@RequestParam(defaultValue = "false") boolean isApproved,
			@RequestParam String province) {

		apt.setApproved(isApproved);
		apt.setStatus(isAvailableNow);
		apt.setProvince(province);

		if (updateApartment(apt)) {
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

		if (!apt.getStreet().isBlank() && !foundApt.getStreet().equals(foundApt.getStreet())) {
			foundApt.setStreet(apt.getStreet());
			isAnythingToUpdate = true;
		}

		if (!apt.getCity().isBlank() && !foundApt.getCity().equals(foundApt.getCity())) {
			foundApt.setCity(apt.getCity());
			isAnythingToUpdate = true;
		}

		if (!apt.getPostalCode().isBlank() && !foundApt.getPostalCode().equals(foundApt.getPostalCode())) {
			foundApt.setPostalCode(apt.getPostalCode());
			isAnythingToUpdate = true;
		}

		if (!apt.getPropertyManager().isBlank()
				&& !foundApt.getPropertyManager().equals(foundApt.getPropertyManager())) {
			foundApt.setPropertyManager(apt.getPropertyManager());
			isAnythingToUpdate = true;
		}

		if (!foundApt.isStatus() != foundApt.isStatus()) {
			foundApt.setStatus(apt.isStatus());
			isAnythingToUpdate = true;
		}
		
		if (!foundApt.isApproved() != foundApt.isApproved()) {
			foundApt.setApproved(apt.isApproved());
			isAnythingToUpdate = true;
		}

		if (apt.getRentFrom() != null && foundApt.getRentFrom() != apt.getRentFrom()) {
			foundApt.setRentFrom(apt.getRentFrom());
			isAnythingToUpdate = true;
		}

		if (apt.getRentTo() != null && foundApt.getRentTo() != apt.getRentTo()) {
			foundApt.setRentTo(apt.getRentTo());
			isAnythingToUpdate = true;
		}
		if(apt.getRent() != null) {
			foundApt.setRent(apt.getRent());
			isAnythingToUpdate = true;
		}
		if (isAnythingToUpdate) {
			apartmentRepo.save(foundApt);
		}

		return isAnythingToUpdate;
	}

	private boolean isAptValidated(Apartment apt) {
		boolean isValidated = true;

		if (apt.getStreet().isBlank())
			isValidated = false;
		if (apt.getCity().isBlank())
			isValidated = false;
		if (apt.getProvince().isBlank())
			isValidated = false;
		if (apt.getPostalCode().isBlank())
			isValidated = false;
		if (apt.getPropertyManager().isBlank())
			isValidated = false;
		if (apt.getRentFrom() == null)
			isValidated = false;
		if (apt.getRent() == null)
			isValidated = false;
		if (apt.getRentTo() == null)
			isValidated = false;

		return isValidated;
	}

	private String encryptWithMD5(String passwordToHash) {
		String generatedPassword = null;
		try {
			// Create MessageDigest instance for MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			// Add password bytes to digest
			md.update(passwordToHash.getBytes());
			// Get the hash's bytes
			byte[] bytes = md.digest();
			// This bytes[] has bytes in decimal format;
			// Convert it to hexadecimal format
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			// Get complete hashed password in hex format
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return generatedPassword;
	}
	private void sendEmail(String to, String subject, String body) {
		// TODO Auto-generated method stub
		System.out.println("Sending your message!");
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(to);
		msg.setSubject(subject);	
		msg.setText(body);
		try {
			javaMailSender.send(msg);
		}
		catch(Exception ex) {
			ex.toString();
		}
		System.out.println("Done!");
	}
	
	private String summaryApt(String name) {
		return "Hello " + name + ",\n\n" +
				"You have booked this Apartment:\n" + 
				"Apartment No: " + currentApt.getApartmentNo() +
				" at: " + currentApt.getAddress() + 
				"\n\n The rent will be: $" + currentApt.getRent() +
				"\n\n Your property manager will be: " +currentApt.getPropertyManager()+
				"\n\n The period for the rent is from: " +currentApt.getRentFrom()+
				" to " +currentApt.getRentTo()+
				"\n\n Here is an image of the apartment:\n" + currentApt.getImageURL()+
				"\n\nRegards,\n"+currentApt.getPropertyManager();
	}
}