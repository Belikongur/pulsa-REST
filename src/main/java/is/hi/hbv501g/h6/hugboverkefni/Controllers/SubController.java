package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Sub;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
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
public class SubController {

    private final PostServiceImplementation postService;
    private final SubServiceImplementation subService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImplementation userService;

    @Autowired
    public SubController(PostServiceImplementation postService,
                         SubServiceImplementation subService,
                         CloudinaryService cloudinaryService,
                         UserServiceImplementation userService) {
        this.postService = postService;
        this.subService = subService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @RequestMapping(value = "/api/p/", method = RequestMethod.GET)
    public ResponseEntity<List<Sub>> subIndex() {
        List<Sub> allSubs = subService.getSubs();

        return new ResponseEntity<>(allSubs, HttpStatus.OK);
    }

    @RequestMapping(value = "/p/{slug}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> subPage(@PathVariable("slug") String slug) {
        Sub sub = subService.getSubBySlug(slug);
        List<Post> posts = postService.getSubPostsOrderedByCreated(sub);

        Map<String, Object> map = new HashMap<>();

        map.put("sub", sub);
        map.put("posts", posts);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "/newSub", method = RequestMethod.POST)
    public String newSubPOST(String name, @RequestParam("image") MultipartFile image, Model model) {
        if(name.isEmpty()) {
            model.addAttribute("emptyName", true);
            return "subIndex";
        }
        Sub newSub = new Sub(name);
        if (subService.getSubBySlug(newSub.getSlug()) != null) return "subNameDuplicate";
        String imgUrl = "";
        if (!image.isEmpty()) imgUrl = cloudinaryService.uploadImage(image);
        newSub.setImage(imgUrl);
        subService.addNewSub(newSub);
        return "redirect:/p/" + newSub.getSlug();
    }

    @RequestMapping(value = "/p/{slug}/toggleFollow", method = RequestMethod.POST)
    public String toggleFollow(@PathVariable("slug") String slug, HttpSession session, Model model) {
        Optional<User> user = userService.getUserByUserName(((User) session.getAttribute("user")).getUserName());
        Sub sub = subService.getSubBySlug(slug);
        if (!user.get().isFollowing(sub)) { userService.addSub(user.get(), sub); }
        else { userService.removeSub(user.get(), sub); }
        return "redirect:/p/" + slug;
    }
}
