package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Controllers.BaseController;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Payload.Response.ContentWithUserDetailsResponse;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1/")
public class RestPostController extends BaseController {
    AuthenticationManager authenticationManager;
    @Autowired
    public RestPostController(PostServiceImplementation postService,
                              UserServiceImplementation userService,
                              ReplyServiceImplementation replyService,
                              VoteServiceImplementation voteService,
                              SubServiceImplementation subService,
                              CloudinaryService cloudinaryService,
                              AuthenticationManager authenticationManager) {
        super(postService, userService, replyService, voteService, subService, cloudinaryService);
        this.authenticationManager = authenticationManager;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Post> findAllPosts() {
        List<Post> posts = postService.getPostsOrderedByCreated();
        
        for (Post post : posts) {
            post.getCreator().setPosts(new ArrayList<Post>());
            post.getCreator().setReplies(new ArrayList<Reply>());
            for (Voter voter : post.getVoted()) {
                voter.getUser().setPosts(new ArrayList<Post>());
                voter.getUser().setReplies(new ArrayList<Reply>());
            }
        }
        
        return posts;
    }


    @RequestMapping(value = "p/{slug}/newPost", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public @ResponseBody ResponseEntity createNewPost(@PathVariable String slug,
                                                      @RequestPart String title,
                                                      @RequestPart(name = "text", required = false) String text,
                                                      @RequestPart(name = "image", required = false) MultipartFile image,
                                                      @RequestPart(name = "audio", required = false) MultipartFile audio,
                                                      @RequestPart(name = "recording", required = false) String recording) {
        UserDetailsImplementation userDetails;
        User user;
        if(isAnonRequest()) {
            userDetails = userService.getAnonDetails();
            user = userService.getAnon();
        } else {
            userDetails = getUserDetails();
            user = userService.getUserByUsername(userDetails.getUsername()).get();
        }

        if(text == null) text = "";
        Sub sub = subService.getSubBySlug(slug);
        if(sub == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sub not found with slug: " + slug);

        try {
            Post post = createPost(title, sub, text, image, audio, recording, user);
            postService.addNewPost(post);
            post.getCreator().setPosts(new ArrayList<Post>());
            post.getCreator().setReplies(new ArrayList<Reply>());
            for (Voter voter : post.getVoted()) {
                voter.getUser().setPosts(new ArrayList<Post>());
                voter.getUser().setReplies(new ArrayList<Reply>());
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(post);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @RequestMapping(value = "p/{slug}/{id:\\d+}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity replyPost(@PathVariable String slug,
                                                  @PathVariable long id,
                                                  @RequestPart(name = "text", required = false) String text,
                                                  @RequestPart(name = "image", required = false) MultipartFile image,
                                                  @RequestPart(name = "audio", required = false) MultipartFile audio,
                                                  @RequestPart(name = "recording", required = false) String recording) {
        UserDetailsImplementation userDetails;
        User user;
        if(isAnonRequest()) {
            userDetails = userService.getAnonDetails();
            user = userService.getAnon();
        } else {
            userDetails = getUserDetails();
            user = userService.getUserByUsername(userDetails.getUsername()).get();
        }

        if(text == null) text = "";
        Optional<Post> post = postService.getPostById(id);
        Sub sub = subService.getSubBySlug(slug);
        if(!post.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        if(sub == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Slug not found with slug: " + slug);


        if(text.isEmpty() && image == null && audio == null && recording == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Reply cannot be empty");
        }

        try {
            Reply reply = createReply(text, sub, image, audio, recording, user);
            replyService.addNewReply(reply);
            post.get().addReply(reply);
            postService.addNewPost(post.get());
            
            reply.getCreator().setPosts(new ArrayList<Post>());
            reply.getCreator().setReplies(new ArrayList<Reply>());
            for (Voter voter : reply.getVoted()) {
                voter.getUser().setPosts(new ArrayList<Post>());
                voter.getUser().setReplies(new ArrayList<Reply>());
            }
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(reply);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "p/{slug}/{id}")
    public Post getPost(@PathVariable String slug, @PathVariable long id) {
        return postService.getPostById(id).get();
    }

    @PostMapping("p/{slug}/{postId}/{id}")
    public @ResponseBody ResponseEntity replyReply(@PathVariable String slug,
                                                   @PathVariable long postId,
                                                   @PathVariable long id,
                                                   @RequestPart(name = "text", required = false) String text,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   @RequestPart(name = "audio", required = false) MultipartFile audio,
                                                   @RequestPart(name = "recording", required = false) String recording) {
        UserDetailsImplementation userDetails;
        User user;
        if(isAnonRequest()) {
            userDetails = userService.getAnonDetails();
            user = userService.getAnon();
        } else {
            userDetails = getUserDetails();
            user = userService.getUserByUsername(userDetails.getUsername()).get();
        }

        if(text == null) text = "";
        Optional<Reply> prevReply = replyService.getReplyById(id);
        Sub sub = subService.getSubBySlug(slug);
        if(!prevReply.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        if(sub == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Slug: " + slug + " not found");

        if(text.isEmpty() && image == null && audio == null && recording == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Reply cannot be empty");

        try {
            Reply reply = createReply(text, sub, image, audio, recording, user);
            replyService.addNewReply(reply);
            prevReply.get().addReply(reply);
            replyService.addNewReply(prevReply.get());
            
            reply.getCreator().setPosts(new ArrayList<Post>());
            reply.getCreator().setReplies(new ArrayList<Reply>());
            for (Voter voter : reply.getVoted()) {
                voter.getUser().setPosts(new ArrayList<Post>());
                voter.getUser().setReplies(new ArrayList<Reply>());
            }
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(reply);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    public boolean isAnonRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean requestAnon = authentication instanceof AnonymousAuthenticationToken;
        boolean hasRoleAnon = authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ANON"));

        return requestAnon || hasRoleAnon;
    }

    public UserDetailsImplementation getUserDetails() {
        return (UserDetailsImplementation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
