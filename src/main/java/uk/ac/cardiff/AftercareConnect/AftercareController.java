package uk.ac.cardiff.AftercareConnect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;

@Controller
public class AftercareController {

    @Autowired
    private AftercareRepository aftercareRepository;
    List<Aftercare> facilities;

    @Autowired
    private UserRepository userRepository;

    List<UserAccount> users;

    public Aftercare getAftercareById(int id) {
        facilities = aftercareRepository.getAftercare();
        for (Aftercare facility : facilities) {
            if (facility.getId() == (id)) {
                return facility;
            }
        }
        return null;
    }

    public UserAccount getUserById(int id) {
        users = userRepository.getAllAccounts();
        for (UserAccount user : users) {
            if(user.getId() == (id)) {
                return user;
            }
        }
        return null;
    }

    @GetMapping("/carehome/{id}")
    public ModelAndView aftercare(@PathVariable int id, Authentication authentication) {
        // Fetch the care home details
        Aftercare selectedFacility = getAftercareById(id);

        // Check if the care home is approved
        if (!aftercareRepository.isApproved(id)) {
            return new ModelAndView("redirect:/403"); // Deny access if not approved
        }

        // Prepare the model and view
        ModelAndView modelAndView = new ModelAndView("aftercare");
        modelAndView.addObject("aftercare", selectedFacility);

        // Fetch and add map coordinates
        String postcode = selectedFacility.getAddress();
        modelAndView.addObject("postcode", postcode);

        if (postcode == null || postcode.trim().isEmpty()) {
            addDefaultLocation(modelAndView, "Postcode is missing. Showing default location.");
        } else {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = "https://api.postcodes.io/postcodes/" + postcode;
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                    double latitude = (double) result.get("latitude");
                    double longitude = (double) result.get("longitude");
                    modelAndView.addObject("latitude", latitude);
                    modelAndView.addObject("longitude", longitude);
                } else {
                    addDefaultLocation(modelAndView, "Invalid postcode. Showing default location.");
                }
            } catch (Exception e) {
                addDefaultLocation(modelAndView, "Error fetching location data. Showing default location.");
            }
        }

        return modelAndView;
    }

    private void addDefaultLocation(ModelAndView modelAndView, String errorMessage) {
        modelAndView.addObject("latitude", 51.5074); // Default London latitude
        modelAndView.addObject("longitude", -0.1278); // Default London longitude
        modelAndView.addObject("error", errorMessage);
    }

    @GetMapping("carehome/edit/{id}")
    public ModelAndView editAftercare(@PathVariable int id, Authentication authentication) {
        // Fetch the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Check if the user has the ADMIN role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        // If the user is not an admin, restrict access to their assigned carehome
        if (!isAdmin) {
            int assignedCarehomeId = userRepository.getAssignedCarehomeId(email);
            if (id != assignedCarehomeId) {
                return new ModelAndView("redirect:/403");
            }
        }

        // Fetch and display the carehome details
        ModelAndView modelAndView = new ModelAndView("editAftercare");
        Aftercare selectedFacility = getAftercareById(id);
        modelAndView.addObject("aftercare", selectedFacility);

        //If the carehome is unapproved, then return a page telling them they can't access the care home.
        if (aftercareRepository.isApproved(id) == false){
            modelAndView = new ModelAndView("redirect:/403");
        }

        return modelAndView;
    }

    @GetMapping("/user/edit/{id}")
    public ModelAndView editUser(@PathVariable int id, Authentication authentication) {
        // Get the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch the logged-in user's details from the database
        UserAccount loggedInUser = userRepository.findByEmail(email);

        // Restrict access to the edit page unless the logged-in user's ID matches the requested ID
        if (loggedInUser.getId() != id) {
            return new ModelAndView("redirect:/403");
        }

        // Fetch the user details for the given ID and show the edit page
        UserAccount userToEdit = userRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView("userEdit");
        modelAndView.addObject("user", userToEdit);
        return modelAndView;
    }

    @PostMapping("/editUser")
    public ModelAndView editUser(
            @RequestParam("id") int id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("area") String area,
            Authentication authentication) {

        // Get the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String loggedInEmail = userDetails.getUsername();

        // Fetch the logged-in user's details
        UserAccount loggedInUser = userRepository.findByEmail(loggedInEmail);

        // Restrict access to updating the user profile unless the logged-in user's ID matches the ID in the request
        if (loggedInUser.getId() != id) {
            return new ModelAndView("redirect:/403");
        }

        // Update the user details in the database
        userRepository.updateUser(id, name, email, password, area);

        // Redirect to the user's home/dashboard page after editing
        return new ModelAndView("redirect:/home");
    }

    @PostMapping("editAftercare")
    public ModelAndView update(
            @RequestParam("id") int id,
            @RequestParam("homeName") String homeName,
            @RequestParam("email") String email,
            @RequestParam("phoneNo") String phoneNo,
            @RequestParam("has12HourCare") boolean has12HourCare,
            @RequestParam("speaksWelsh") boolean speaksWelsh,
            @RequestParam("assistedLiving") boolean assistedLiving,
            @RequestParam(value = "generalCareWardTotalBeds", required = false) Integer generalCareTotal,
            @RequestParam(value = "generalCareWardAvailableBeds", required = false) Integer generalCareAvailable,
            @RequestParam(value = "mentalHealthWardTotalBeds", required = false) Integer mentalHealthTotal,
            @RequestParam(value = "mentalHealthWardAvailableBeds", required = false) Integer mentalHealthAvailable,
            @RequestParam(value = "dementiaWardTotalBeds", required = false) Integer dementiaTotal,
            @RequestParam(value = "dementiaWardAvailableBeds", required = false) Integer dementiaAvailable,
            @RequestParam(value = "rehabWardTotalBeds", required = false) Integer rehabTotal,
            @RequestParam(value = "rehabWardAvailableBeds", required = false) Integer rehabAvailable,
            @RequestParam(value = "palliativeWardTotalBeds", required = false) Integer palliativeTotal,
            @RequestParam(value = "palliativeWardAvailableBeds", required = false) Integer palliativeAvailable,
            Authentication authentication) {

        // Fetch the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String loggedInEmail = userDetails.getUsername();

        // Check if the user has the ADMIN role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        // Restrict access for non-admin users
        if (!isAdmin) {
            int assignedCarehomeId = userRepository.getAssignedCarehomeId(loggedInEmail);
            if (id != assignedCarehomeId) {
                return new ModelAndView("redirect:/403");
            }
        }

        int tempGeneralCareTotal = (generalCareTotal != null) ? generalCareTotal : 0;
        int tempMentalHealthTotal = (mentalHealthTotal != null) ? mentalHealthTotal : 0;
        int tempDementiaTotal = (dementiaTotal != null) ? dementiaTotal : 0;
        int tempRehabTotal = (rehabTotal != null) ? rehabTotal : 0;
        int tempPalliativeTotal = (palliativeTotal != null) ? palliativeTotal : 0;

        int newBedTotal = tempGeneralCareTotal + tempMentalHealthTotal + tempDementiaTotal + tempRehabTotal + tempPalliativeTotal;

        // Update the carehome details in the database
        aftercareRepository.updateAftercare(id, homeName, email, phoneNo, has12HourCare, speaksWelsh, assistedLiving,
                newBedTotal, generalCareTotal, generalCareAvailable, mentalHealthTotal, mentalHealthAvailable,
                dementiaTotal, dementiaAvailable, rehabTotal, rehabAvailable, palliativeTotal, palliativeAvailable );

        // Redirect to the carehome page after updating
        return new ModelAndView("redirect:/carehome/" + id);
    }

    @GetMapping("/admin/carehome/edit")
    public ModelAndView showAdminEditCarehomePage() {
        ModelAndView modelAndView = new ModelAndView("adminEditCarehome");
        List<Aftercare> approvedCarehomes = aftercareRepository.getApprovedCarehomes();
        modelAndView.addObject("carehomes", approvedCarehomes);
        return modelAndView;
    }

    @GetMapping("/admin/carehome/edit/redirect")
    public String redirectToCarehomeEdit(@RequestParam("carehomeId") int carehomeId) {
        return "redirect:/carehome/edit/" + carehomeId;
    }

    @GetMapping("/contactUs")
    public ModelAndView showcontactUs() {
        ModelAndView modelAndView = new ModelAndView("contactUs");
        return modelAndView;
    }

    @Autowired
    private ViewMessageRepository viewMessageRepository; // Ensure this is autowired

    @PostMapping("/contactUs")
    public String handleContactUs(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("userType") String userType,
            @RequestParam("message") String message,
            Model model) {

        try {
            // Validate full name
            if (fullName == null || fullName.trim().isEmpty()) {
                throw new IllegalArgumentException("Full Name is required.");
            }
            if (fullName.length() < 2 || fullName.length() > 30) {
                throw new IllegalArgumentException("Full Name must be between 2 and 30 characters.");
            }

            // Validate email
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required.");
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Please enter a valid email address.");
            }
            if (email.length() > 50) {
                throw new IllegalArgumentException("Email must not exceed 50 characters.");
            }

            // Validate user type
            if (userType == null || userType.trim().isEmpty()) {
                throw new IllegalArgumentException("User Type is required.");
            }
            List<String> validUserTypes = Arrays.asList("Social Services", "Society Manager", "Carehome Manager", "Stakeholder");
            if (!validUserTypes.contains(userType)) {
                throw new IllegalArgumentException("Invalid User Type.");
            }

            // Validate message
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message is required.");
            }
            int charLength = message.length();
            if (charLength < 10) {
                throw new IllegalArgumentException("Message must include at least 10 characters.");
            }
            if (charLength > 1200) {
                throw new IllegalArgumentException("Message must not exceed 1,200 characters.");
            }

            // If all validations pass, create a new ViewMessage object
            ViewMessage viewMessage = new ViewMessage(0, fullName, email, userType, message);

            // Save the message to the database
            viewMessageRepository.saveMessage(viewMessage);

            // Redirect to confirmation page with success query parameter
            return "redirect:/contactUsConfirmation?success=true";
        } catch (IllegalArgumentException e) {
            // Add the error message to the model to display on the form
            model.addAttribute("errorMessage", e.getMessage());
            return "contactUs"; // Return to the form with validation errors
        }
    }

    @GetMapping("/admin/messages")
    public String viewMessages(Model model) {
        // Fetch all messages from the database
        List<ViewMessage> messages = viewMessageRepository.getAllMessages();
        model.addAttribute("messages", messages); // Add the messages to the model
        return "viewMessages"; // Return the name of the Thymeleaf template (viewMessages.html)
    }

    @GetMapping("/viewMessage/{id}")
    @ResponseBody
    public ViewMessage getMessageDetails(@PathVariable int id) {
        return viewMessageRepository.getMessageById(id); // Fetch message details from the database
    }

    @PostMapping("/deleteMessage/{id}")
    @ResponseBody
    public void deleteMessage(@PathVariable int id) {
        viewMessageRepository.deleteMessage(id); // Delete the message from the database
    }

    @GetMapping("/contactUsConfirmation")
    public String contactUsConfirmation(@RequestParam(value = "success", required = false) String success) {
        if (success == null || !success.equals("true")) {
            return "redirect:/contactUs"; // Redirect if no valid submission
        }
        return "contactUsConfirmation"; // Show confirmation page
    }

    @GetMapping("/home")
    public String getHome(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String usersRole = authorities.iterator().next().getAuthority();
        switch (usersRole) {
            case "ROLE_STAKEHOLDER":
                return "redirect:/stakeholder/home";
            case "ROLE_SOCIALSERVICE":
                return "redirect:/socialService/home";
            case "ROLE_SOCIETYMANAGER":
                return "redirect:/societyManager/home";
            case "ROLE_CAREHOMEMANAGER":
                return "redirect:/careHomeManager/home";
            case "ROLE_ADMIN":
                return "redirect:/admin/home";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/admin/home")
    public ModelAndView showadminDashboard() {
        ModelAndView modelAndView = new ModelAndView("adminDashboard");
        return modelAndView;
    }

    @GetMapping("/careHomeManager/home")
    public ModelAndView showCarehomeDashboard(Authentication authentication) {
        // Fetch the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch the assignedCarehomeId for this carehome manager
        int assignedCarehomeId = userRepository.getAssignedCarehomeId(email);

        if (assignedCarehomeId == -1) {
            // Handle the case where no carehome is assigned
            return new ModelAndView("redirect:/home");
        }

        // Fetch user details from repository
        UserAccount userAccount = userRepository.findByEmail(email);

        ModelAndView modelAndView = new ModelAndView("carehomeDashboard");
        modelAndView.addObject("assignedCarehomeId", assignedCarehomeId); // Pass it to the view
        modelAndView.addObject("userId", userAccount.getId());
        return modelAndView;
    }

    @GetMapping("/socialService/home")
    public ModelAndView showSocialServiceDashboard(Authentication authentication) {
        // Fetch logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch user details from repository
        UserAccount userAccount = userRepository.findByEmail(email);

        // Add userId to the model
        ModelAndView modelAndView = new ModelAndView("socialserviceDashboard");
        modelAndView.addObject("userId", userAccount.getId());
        return modelAndView;
    }

    @GetMapping("/societyManager/home")
    public ModelAndView showSocietyManagerDashboard(Authentication authentication) {
        // Fetch logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch user details from repository
        UserAccount userAccount = userRepository.findByEmail(email);

        // Add userId to the model
        ModelAndView modelAndView = new ModelAndView("societymanagerDashboard");
        modelAndView.addObject("userId", userAccount.getId());
        return modelAndView;
    }

    @GetMapping("/stakeholder/home")
    public ModelAndView showStakeholderDashboard(Authentication authentication) {
        // Fetch logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch user details from repository
        UserAccount userAccount = userRepository.findByEmail(email);

        // Add userId to the model
        ModelAndView modelAndView = new ModelAndView("stakeholderDashboard");
        modelAndView.addObject("userId", userAccount.getId());
        return modelAndView;
    }

    @GetMapping("/carehome/add")
    public ModelAndView addNewCarehome(){
        ModelAndView modelAndView = new ModelAndView("createCarehome");
        return modelAndView;
    }

    @PostMapping("/addCarehome")
    public ModelAndView addCarehome(
            @RequestParam("homeName") String homeName,
            @RequestParam("address") String address,
            @RequestParam("email") String email,
            @RequestParam("phoneNo") String phoneNo,
            @RequestParam("area") String area,
            @RequestParam("has12HourCare") boolean has12HourCare,
            @RequestParam("speaksWelsh") boolean speaksWelsh,
            @RequestParam("assistedLiving") boolean assistedLiving,
            //Set it so the values are not always required
            //Also Integer allows null values while int does not
            @RequestParam(value = "generalCareWardTotalBeds", required = false) Integer generalCareTotal,
            @RequestParam(value = "generalCareWardAvailableBeds", required = false) Integer generalCareAvailable,
            @RequestParam(value = "mentalHealthWardTotalBeds", required = false) Integer mentalHealthTotal,
            @RequestParam(value = "mentalHealthWardAvailableBeds", required = false) Integer mentalHealthAvailable,
            @RequestParam(value = "dementiaWardTotalBeds", required = false) Integer dementiaTotal,
            @RequestParam(value = "dementiaWardAvailableBeds", required = false) Integer dementiaAvailable,
            @RequestParam(value = "rehabWardTotalBeds", required = false) Integer rehabTotal,
            @RequestParam(value = "rehabWardAvailableBeds", required = false) Integer rehabAvailable,
            @RequestParam(value = "palliativeWardTotalBeds", required = false) Integer palliativeTotal,
            @RequestParam(value = "palliativeWardAvailableBeds", required = false) Integer palliativeAvailable) {

        if(userRepository.checkForEmailAftercare(email)) {
            ModelAndView error = new ModelAndView("createCarehome");
            error.addObject("errorMessage",
                    "This email is already in use by another care home. Please enter a new email to register");
            return error;
        }

        int tempGeneralCareTotal = 0;
        if (generalCareTotal != null){
            tempGeneralCareTotal = generalCareTotal;
        }

        int tempMentalHealthTotal = 0;
        if (mentalHealthTotal != null){
            tempMentalHealthTotal = mentalHealthTotal;
        }

        int tempDementiaTotal = 0;
        if (dementiaTotal != null){
            tempDementiaTotal = dementiaTotal;
        }

        int tempRehabTotal = 0;
        if (rehabTotal != null){
            tempRehabTotal = rehabTotal;
        }

        int tempPalliativeTotal = 0;
        if (palliativeTotal != null){
            tempPalliativeTotal = palliativeTotal;
        }

        int totalNumOfBeds = tempGeneralCareTotal + tempMentalHealthTotal + tempDementiaTotal + tempRehabTotal + tempPalliativeTotal;


        aftercareRepository.addCarehome(
                homeName,
                address,
                email,
                phoneNo,
                area,
                has12HourCare,
                speaksWelsh,
                assistedLiving,
                totalNumOfBeds,
                generalCareTotal,
                generalCareAvailable,
                mentalHealthTotal,
                mentalHealthAvailable,
                dementiaTotal,
                dementiaAvailable,
                rehabTotal,
                rehabAvailable,
                palliativeTotal,
                palliativeAvailable);

        ModelAndView modelAndView = new ModelAndView("redirect:/carehomeThanks?success=true");
        return modelAndView;
    }

    @GetMapping("/carehomeThanks")
    public String registerCarehomeThanks(@RequestParam(value = "success", required = false) String success) {
        if (success == null || !success.equals("true")) {
            return "redirect:/carehome/add"; // Redirect to the registration form
        }
        return "registercarehomeThanks"; // Show the confirmation page
    }

    @GetMapping("/carehome/update/{id}")
    public ModelAndView showUpdateBeds(@PathVariable int id, Authentication authentication) {
        // Fetch the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch the assignedCarehomeId for this carehome manager
        int assignedCarehomeId = userRepository.getAssignedCarehomeId(email);

        // Restrict access if the IDs don't match
        if (id != assignedCarehomeId) {
            return new ModelAndView("redirect:/403");
        }

        ModelAndView modelAndView = new ModelAndView("updateBeds");
        Aftercare updatedFacility = getAftercareById(id);
        modelAndView.addObject("aftercare", updatedFacility);

        if (aftercareRepository.isApproved(id) == false){
            modelAndView = new ModelAndView("redirect:/403");
        }

        return modelAndView;
    }

    @PostMapping("/updateBedNum")
    public ModelAndView updateAvailableBeds(
            @RequestParam("facilityId") int id,
            @RequestParam(value = "generalCareWardAvailableBeds", required = false) Integer generalCareAvailable,
            @RequestParam(value = "mentalHealthWardAvailableBeds", required = false) Integer mentalHealthAvailable,
            @RequestParam(value = "dementiaWardAvailableBeds", required = false) Integer dementiaAvailable,
            @RequestParam(value = "rehabWardAvailableBeds", required = false) Integer rehabAvailable,
            @RequestParam(value = "palliativeWardAvailableBeds", required = false) Integer palliativeAvailable,
            Authentication authentication) {

        // Fetch the logged-in user's email
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Fetch the carehome manager's assigned carehome ID
        int assignedCarehomeId = userRepository.getAssignedCarehomeId(email);

        // Restrict access if the IDs don't match
        if (id != assignedCarehomeId) {
            return new ModelAndView("redirect:/403");
        }

        aftercareRepository.updateAvailableBeds(
                id,
                generalCareAvailable,
                mentalHealthAvailable,
                dementiaAvailable,
                rehabAvailable,
                palliativeAvailable
        );

        return new ModelAndView("redirect:/carehome/" + id);
    }

    @GetMapping("/carehome/overview")
    public ModelAndView showBedAreaOverview() {
        ModelAndView modelAndView = new ModelAndView("bedSpaceAreaOverview");
        facilities = aftercareRepository.getAftercare();
        modelAndView.addObject("facilities", facilities);
        return modelAndView;
    }

    @GetMapping("/user/register")
    public ModelAndView userRegForm() {
        ModelAndView modelAndView = new ModelAndView("userRegistration");
        facilities = aftercareRepository.getApprovedCarehomes(); // Fetch only approved carehomes
        modelAndView.addObject("facilities", facilities);
        return modelAndView;
    }

    @PostMapping("/registerUser")
    public ModelAndView registerUser(
            @RequestParam("userType") String userType,
            @RequestParam(name = "assignedCarehomeId", defaultValue = "0", required = false) int assignedCarehomeId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("area") String area
            ) {
        //If usertype != carehomeManager then the assignedCarehomeId is 0 to avoid being accidentally assigned
        if (!userType.equalsIgnoreCase("carehomeManager")) {
            assignedCarehomeId = 0;
        }

        if(userRepository.checkForEmailUserAccounts(email)) {
            ModelAndView error = new ModelAndView("userRegistration");
            error.addObject("errorMessage", "This email is already in use by another account. Please enter a new email to register.");
            facilities = aftercareRepository.getApprovedCarehomes();
            error.addObject("facilities", facilities);
            return error;
        }

        userRepository.addUser(userType, assignedCarehomeId, name, email, password, area);

        ModelAndView modelAndView = new ModelAndView("redirect:/userThanks?success=true");
        return modelAndView;
    }

    @GetMapping("/userThanks")
    public String thanks(@RequestParam(value = "success", required = false) String success) {
        if (success == null || !success.equals("true")) {
            return "redirect:/user/register"; // Redirect to the registration form
        }
        return "registeruserThanks";
    }

    @GetMapping("/admin/incomingCarehomes")
    public ModelAndView carehomeApproval() {
        ModelAndView modelAndView = new ModelAndView("carehomeRegApproval");
        List<Aftercare> newRegs = aftercareRepository.getIncomingRegistrations();
        modelAndView.addObject("newRegistrations", newRegs);
        modelAndView.addObject("aftercareRepo", aftercareRepository);
        return modelAndView;
    }

    @GetMapping("/admin/incomingRegistrations")
    public ModelAndView showRegApproval() {
        ModelAndView modelAndView = new ModelAndView("userRegApproval");
        List<UserAccount> newRegs = userRepository.getIncomingRegistrations();
        modelAndView.addObject("newRegistrations", newRegs);
        modelAndView.addObject("userRepo", userRepository);
        return modelAndView;
    }

    @GetMapping("/societyManager/incomingRegistrations")
    public ModelAndView showAreaRegApproval(Authentication authentication) {
        Object currentUser = authentication.getPrincipal();
        UserDetails userDetails = (UserDetails) currentUser;
        String username = userDetails.getUsername();
        ModelAndView modelAndView = new ModelAndView("userRegApproval");
        List<UserAccount> newRegs = userRepository.getIncomingAreaRegistrations(username);
        modelAndView.addObject("newRegistrations", newRegs);
        modelAndView.addObject("userRepo", userRepository);
        return modelAndView;
    }

    @PostMapping("/carehome/approve")
    @ResponseBody
    public void approveNewCarehome(@RequestParam("email") String email) {
        aftercareRepository.approveCarehome(email);
    }

    @PostMapping("/carehome/decline")
    @ResponseBody
    public void declineNewCarehome(@RequestParam("email") String email) {
        aftercareRepository.removeTempCarehome(email);
    }


    @PostMapping("/approveUser")
    @ResponseBody
    public void approveNewUser(@RequestParam("email") String email) {
        userRepository.approveUser(email);
    }

    @PostMapping("/declineUser")
    @ResponseBody
    public void declineNewUser(@RequestParam("email") String email) {
        userRepository.removeTempUser(email);
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

    @GetMapping("/carehome/search")
    public ModelAndView showSearchPage() {
        ModelAndView modelAndView = new ModelAndView("filterPage");
        return modelAndView;
    }

    // Handle filtering
    @PostMapping("/filter")
    @ResponseBody
    public List<Aftercare> filterCareHomes(
    @RequestParam(value = "homeNameSearch", required = false) String homeName,
    @RequestParam(value = "areaDropdown", required = false) String area,
    @RequestParam(value = "has12HourCare", required = false) Boolean has12HourCare,
    @RequestParam(value = "speaksWelsh", required = false) Boolean speaksWelsh,
    @RequestParam(value = "assistedLiving", required = false) Boolean assistedLiving,
    @RequestParam(value = "hasGeneralWard", required = false) Boolean hasGeneralWard,
    @RequestParam(value = "hasMentalWard", required = false) Boolean hasMentalWard,
    @RequestParam(value = "hasDementiaWard", required = false) Boolean hasDementiaWard,
    @RequestParam(value = "hasRehabilitationWard", required = false) Boolean hasRehabilitationWard,
    @RequestParam(value = "hasPalliativeWard", required = false) Boolean hasPalliativeWard) {

        return aftercareRepository.filterAftercare(homeName, area, Boolean.TRUE.equals(has12HourCare),
                Boolean.TRUE.equals(speaksWelsh), Boolean.TRUE.equals(assistedLiving),
                Boolean.TRUE.equals(hasGeneralWard), Boolean.TRUE.equals(hasMentalWard),
                Boolean.TRUE.equals(hasDementiaWard), Boolean.TRUE.equals(hasRehabilitationWard),
                Boolean.TRUE.equals(hasPalliativeWard)); // Return updated page
    }
}

