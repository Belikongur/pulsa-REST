package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.RoleRepository;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.UserRepository;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.UserDetailsImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.ReplyServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.ERole;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Role;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/")
public class RestUserController {

    AuthenticationManager authenticationManager;
    private final UserServiceImplementation userService;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PostServiceImplementation postService;
    private final SubServiceImplementation subService;
    private final ReplyServiceImplementation replyService;
    private final RoleRepository roleRepository;
    PasswordEncoder encoder;
    JwtUtils jwtUtils;

    @Autowired
    public RestUserController(UserServiceImplementation userService,
                              CloudinaryService cloudinaryService,
                              PostServiceImplementation postService,
                              SubServiceImplementation subService,
                              ReplyServiceImplementation replyService,
                              RoleRepository roleRepository,
                              JwtUtils jwtUtils,
                              PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager,
                              UserRepository userRepository) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.postService = postService;
        this.subService = subService;
        this.userRepository = userRepository;
        this.replyService = replyService;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.encoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity registerPOST(@Valid User user) {
        if(userService.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error: Username already taken");
        }

        if(userService.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error: Email already taken");
        }

        user.setAvatar("https://res.cloudinary.com/dc6h0nrwk/image/upload/v1667864633/ldqgfkftspzy5yeyzube.png");
        user.setCreated();
        user.setUpdated();
        User newUser = new User(user.getUsername(), encoder.encode(user.getPassword()), user.getRealName(), user.getAvatar(), user.getEmail());
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
        roles.add(userRole);
        newUser.setRoles(roles);

        try {
            userRepository.save(newUser);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }



    }

    @RequestMapping(value = {"login"}, method = RequestMethod.POST)
    public @ResponseBody ResponseEntity authenticateUser(@RequestPart String username,
                                                         @RequestPart String password) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtils.generateJwtToken(auth);

        UserDetailsImplementation userDetails = (UserDetailsImplementation) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        User user = getUserFromUserDetails(userDetails);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        jwt
                )
                .body(user);
    }
/*
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity logout() {
        SecurityContextHolder.clearContext();
        return new ResponseEntity(HttpStatus.OK);
    }
*/
    @RequestMapping(value = "user/{id}/edit", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER') and #id == principal.getId()")
    public @ResponseBody ResponseEntity editAccountPOST(@PathVariable("id") long id, @RequestPart(value = "realName", required = false) String realName) {
        if(realName == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("real name cannot be empty");

        UserDetails userDetails = getUserDetails();
        User user = getUserFromUserDetails(userDetails);
        if (realName.equals(user.getRealName())) return ResponseEntity.status(HttpStatus.OK).body(realName);
        user.setRealName(realName);
        userService.editRealName(user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @RequestMapping(value = "user/{id}/edit/avatar", method = RequestMethod.POST)
    @PreAuthorize("hasRole('USER') and #id == principal.getId()")
    public @ResponseBody ResponseEntity changeAvatarPOST(@PathVariable("id") long id, @RequestPart MultipartFile avatar) {
        UserDetails userDetails = getUserDetails();
        User user = getUserFromUserDetails(userDetails);

        String avatarUrl = cloudinaryService.securify(cloudinaryService.uploadImage(avatar));
        user.setAvatar(avatarUrl);
        userService.editAvatar(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @RequestMapping(value = "user/{id}/edit/username", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER') and #id == principal.getId()")
    public @ResponseBody ResponseEntity changeUsernamePOST(@PathVariable("id") long id, @RequestPart String username) {
        UserDetailsImplementation userDetails = getUserDetails();
        User user = getUserFromUserDetails(userDetails);

        try {
            userService.editUserName(user);
        } catch (DuplicateKeyException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        user.setUsername(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @RequestMapping(value = "user/{id}/edit/password", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER') and #id == principal.getId()")
    public @ResponseBody ResponseEntity changePasswordPOST(@PathVariable("id") long id, @RequestPart String password) {
        UserDetails userDetails = getUserDetails();
        User user = getUserFromUserDetails(userDetails);
        user.setPassword(password);
        userService.editPassword(user);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "user/{id}/edit/email", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER') and #id == principal.getId()")
    public @ResponseBody ResponseEntity changeEmailPOST(@PathVariable("id") long id, @RequestPart String email) {
        UserDetails userDetails = getUserDetails();
        User user = getUserFromUserDetails(userDetails);
        user.setEmail(email);

        try {
            userService.editEmail(user);
        } catch (DuplicateKeyException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @RequestMapping(value = "u/{username}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity userPageGET(@PathVariable("username") String username) {
        User user = userService.getUserObjectByUserName(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    public UserDetailsImplementation getUserDetails() {
        return (UserDetailsImplementation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User getUserFromUserDetails(UserDetails userDetails) {
        return userService.getUserByUsername(userDetails.getUsername()).get();
    }


}
