package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.ERole;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Sub;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class RestSubController {
    AuthenticationManager authenticationManager;
    private final PostServiceImplementation postService;
    private final SubServiceImplementation subService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImplementation userService;

    @Autowired
    public RestSubController(PostServiceImplementation postService,
                             SubServiceImplementation subService,
                             CloudinaryService cloudinaryService,
                             UserServiceImplementation userService,
                             AuthenticationManager authenticationManager) {
        this.postService = postService;
        this.subService = subService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/p", method = RequestMethod.GET)
    public List<Sub> subIndex() {
        return subService.getSubs();
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/p/{slug}", method = RequestMethod.GET)
    public List<Post> subPage(@PathVariable("slug") String slug) {
        Sub sub = subService.getSubBySlug(slug);
        return postService.getSubPostsOrderedByCreated(sub);
    }

    @RequestMapping(value = "/newSub", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public @ResponseBody ResponseEntity newSubPOST(@RequestPart String name,
                                                   @RequestPart(value = "image", required = false) MultipartFile image) {
        if(name.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Sub must contain name");
        }

        Sub newSub = new Sub(name);
        if (subService.getSubBySlug(newSub.getSlug()) != null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Sub name already exists");
        }

        String imgUrl = "";
        if (image != null) {
            try {
                imgUrl = cloudinaryService.uploadImage(image);
                newSub.setImage(imgUrl);
            }catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e.getMessage());
            }
        }

        try {
            subService.addNewSub(newSub);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newSub);
        }catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    @RequestMapping(value = "/p/{slug}/toggleFollow", method = RequestMethod.POST)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity toggleFollow(@PathVariable("slug") String slug) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
        if(!user.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        Sub sub = subService.getSubBySlug(slug);
        if(sub == null) return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Sub not found");

        if (!user.get().isFollowing(sub)) { userService.addSub(user.get(), sub); }
        else { userService.removeSub(user.get(), sub); }
        return new ResponseEntity(HttpStatus.OK);
    }
}
