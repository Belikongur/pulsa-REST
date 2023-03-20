package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Sub;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.hibernate.mapping.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class RestSubController {

    private final PostServiceImplementation postService;
    private final SubServiceImplementation subService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImplementation userService;

    @Autowired
    public RestSubController(PostServiceImplementation postService,
                         SubServiceImplementation subService,
                         CloudinaryService cloudinaryService,
                         UserServiceImplementation userService) {
        this.postService = postService;
        this.subService = subService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
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

    @RequestMapping(value = "/newSub", method = RequestMethod.POST)
    public ResponseEntity newSubPOST(@RequestBody Map<String, Object> map) {
        String name = "", imgUrl = "";
        MultipartFile image = null;
        if(map.containsKey("name")) name = map.get("name").toString();
        if(map.containsKey("image")) image = (MultipartFile) map.get("image");
        System.out.println("Contents of map");
        System.out.println(map);
        map.forEach((field, value)-> {
            System.out.println(field + ": " + value);
        });
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

        if (image != null) {
            try {
                imgUrl = cloudinaryService.uploadImage(image);
                newSub.setImage(imgUrl);
            }catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e);
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
                    .body(e);
        }
    }

    @RequestMapping(value = "/p/{slug}/toggleFollow", method = RequestMethod.POST)
    public ResponseEntity toggleFollow(@PathVariable("slug") String slug, HttpSession session) {
        Optional<User> user = userService.getUserByUserName(((User) session.getAttribute("user")).getUserName());
        if(!user.isPresent()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Must be logged in");
        Sub sub = subService.getSubBySlug(slug);
        if (!user.get().isFollowing(sub)) { userService.addSub(user.get(), sub); }
        else { userService.removeSub(user.get(), sub); }
        return new ResponseEntity(HttpStatus.OK);
    }
}
