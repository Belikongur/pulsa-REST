package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.PostServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.ReplyServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.SubServiceImplementation;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {
    private PostServiceImplementation postService;
    private UserServiceImplementation userService;
    private ReplyServiceImplementation replyService;
    private SubServiceImplementation subService;

    @Autowired
    public PostController(PostServiceImplementation postService,
                          UserServiceImplementation userService,
                          ReplyServiceImplementation replyService,
                          SubServiceImplementation subService){
        this.postService = postService;
        this.userService = userService;
        this.replyService = replyService;
        this.subService = subService;
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.GET)
    public String newPostGET(Post post){

        return "newPost";
    }

    @RequestMapping(value = "/newPost", method = RequestMethod.POST)
    public String newPostPOST(Content c, Post p, BindingResult result, Model model){
        User user = userService.getUsers().get(0);
        Sub sub = subService.getSubs().get(0);
        Post newPost = new Post(p.getTitle(),
                                sub,
                                c,
                                user,
                                new ArrayList<Voter>(),
                                new ArrayList<Reply>());
        postService.addNewPost(newPost);
        return "redirect:/post/" + newPost.getPostId();
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.GET)
    public String postPage(@PathVariable("id") long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if(!post.isPresent()) return "postNotFound";

        List<Reply> postReplies = post.get().getReplies();
        List<Reply> allReplies = new ArrayList<Reply>();
        postReplies.forEach(item -> {
            Optional<Reply> reply = replyService.getReplyById(item.getReplyId());
            if(reply.isPresent()) allReplies.add(reply.get());
        });

        model.addAttribute("post", post.get());
        model.addAttribute("allReplies", allReplies);
        model.addAttribute("reply", new Reply());
        return "postPage";
    }

    @RequestMapping(value = "/post/{id}", method = RequestMethod.POST)
    public String replyPost(@PathVariable("id") long id, Reply reply, Content content, BindingResult result, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if(!post.isPresent()) return "postNotFound";
        reply.setContent(content);
        replyService.addNewReply(reply);
        post.get().addReply(reply);
        postService.addNewPost(post.get());
        return "redirect:/post/" + post.get().getPostId();
    }
}
