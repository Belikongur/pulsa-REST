package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.ReplyServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class HomePageController {
    private final PostServiceImplementation postService;
    private final UserServiceImplementation userService;
    private final ReplyServiceImplementation replyService;

    @Autowired
    public HomePageController(PostServiceImplementation postService, UserServiceImplementation userService, ReplyServiceImplementation replyService) {
        this.postService = postService;
        this.userService = userService;
        this.replyService = replyService;
    }

    @RequestMapping("/api")
    public ResponseEntity<List<Post>> frontPage(Model model) {
        List<Post> posts = postService.getPostsOrderedByCreated();
        if(posts.isEmpty()) return new ResponseEntity(HttpStatus.NOT_FOUND);
        return new ResponseEntity<List<Post>>(posts, HttpStatus.OK);
    }
}