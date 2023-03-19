package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Reply;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Sub;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.ReplyServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class UserController {

    private final UserServiceImplementation userService;

    private final CloudinaryService cloudinaryService;
    private final PostServiceImplementation postService;
    private final SubServiceImplementation subService;
    private final ReplyServiceImplementation replyService;
    @Autowired
    public UserController(UserServiceImplementation userService,
                          CloudinaryService cloudinaryService,
                          PostServiceImplementation postService,
                          SubServiceImplementation subService,
                          ReplyServiceImplementation replyService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.postService = postService;
        this.subService = subService;
        this.replyService = replyService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPOST(@Valid User user, BindingResult result) {
        user.setAvatar("https://res.cloudinary.com/dc6h0nrwk/image/upload/v1667864633/ldqgfkftspzy5yeyzube.png");
        user.setCreated();
        user.setUpdated();

        userService.addNewUser(user, result);

        if (result.hasErrors()) {
            return "register";
        }

        return "redirect:/registrationSuccess";
    }

    @RequestMapping(value = {"/login"}, method = RequestMethod.POST)
    public String loginPOST(User user, HttpSession session) {
        User auth = userService.loginUser(user);

        if (auth == null) return "redirect:login?error";

        // Session time limit er 1800s eða 30m
        // Breyta session time limit -> session.setMaxInactiveInterval(sec);
        session.setAttribute("user", auth);

        return "redirect:/";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        if (session.getAttribute("user") != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/user/{id}/edit", method = RequestMethod.POST)
    public String editAccountPOST(String realName, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (realName.equals(user.getRealName())) return "editAccount";

        user.setRealName(realName);
        userService.editRealName(user);
        model.addAttribute("updated", true);

        return "editAccount";
    }

    @RequestMapping(value = "/user/{id}/edit/avatar", method = RequestMethod.POST)
    public String changeAvatarPOST(@RequestParam MultipartFile avatar, HttpSession session, Model model) {
        if (avatar.isEmpty()) {
            model.addAttribute("avatar", true);
            return "editAccountAvatar";
        }

        User user = (User) session.getAttribute("user");
        String avatarUrl = cloudinaryService.securify(cloudinaryService.uploadImage(avatar));
        user.setAvatar(avatarUrl);
        userService.editAvatar(user);

        return "editAccountAvatar";
    }

    @RequestMapping(value = "/user/{id}/edit/username", method = RequestMethod.POST)
    public String changeUsernamePOST(@ModelAttribute("username") String username, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (username.isBlank()) return "editAccountUsername";

        user.setUserName(username);
        try {
            userService.editUserName(user);
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "Username taken");
            return "editAccountUsername";
        }

        model.addAttribute("updated", true);
        user.setUserName(username);

        return "editAccountUsername";
    }

    @RequestMapping(value = "/user/{id}/edit/password", method = RequestMethod.POST)
    public String changePasswordPOST(String password, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (password.isBlank()) return "editAccountPassword";

        model.addAttribute("updated", true);
        user.setPassword(password);
        userService.editPassword(user);

        return "editAccountPassword";
    }

    @RequestMapping(value = "/user/{id}/edit/email", method = RequestMethod.POST)
    public String changeEmailPOST(String email, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (email.isBlank()) return "editAccountEmail";

        user.setEmail(email);
        try {
            userService.editEmail(user);
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "Email in use");
            return "editAccountEmail";
        }

        model.addAttribute("updated", true);
        return "editAccountEmail";
    }

    @RequestMapping(value = "/api/u/{username}")
    public ResponseEntity<Map<String, Object>> userPageGET(@PathVariable("username") String username) {
        Optional<User> theUser = userService.getUserByUserName(username);

        if (!theUser.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        User user = userService.getUserObjectByUserName(username);
        List<Post> posts = postService.getPostsByUser(user);
        List<Sub> subs = user.getSubs();
        List<Reply> replies = replyService.getRepliesByUser(user);

        Map<String, Object> map = new HashMap<>();

        map.put("user", user);
        map.put("posts", posts);
        map.put("subs", subs);
        map.put("replies", replies);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}



