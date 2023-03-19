package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.ReplyServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RestPostController {
    private final PostServiceImplementation postService;
    private final UserServiceImplementation userService;
    private final ReplyServiceImplementation replyService;

    @Autowired
    public RestPostController(PostServiceImplementation postServiceImplementation, UserServiceImplementation userServiceImplementation, ReplyServiceImplementation replyServiceImplementation) {
        this.postService = postServiceImplementation;
        this.userService = userServiceImplementation;
        this.replyService = replyServiceImplementation;
    }

    @GetMapping
    public List<Post> findAllPosts() {
        return postService.getPostsOrderedByCreated();
    }




}
