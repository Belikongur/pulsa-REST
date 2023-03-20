package is.hi.hbv501g.h6.hugboverkefni.Controllers.RestAPI;

import is.hi.hbv501g.h6.hugboverkefni.Controllers.BaseController;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Post;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Reply;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Sub;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.*;
import org.hibernate.mapping.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class RestPostController extends BaseController {

    @Autowired
    public RestPostController(PostServiceImplementation postService,
                              UserServiceImplementation userService,
                              ReplyServiceImplementation replyService,
                              VoteServiceImplementation voteService,
                              SubServiceImplementation subService,
                              CloudinaryService cloudinaryService) {
        super(postService, userService, replyService, voteService, subService, cloudinaryService);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Post> findAllPosts() {
        return postService.getPostsOrderedByCreated();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/p/{slug}/{id}")
    public Post getPost(@PathVariable String slug, @PathVariable long id) {
        return postService.getPostById(id).get();
    }

    @PostMapping(value = "/p/{slug}/newPost")
    public ResponseEntity createNewPost(@PathVariable String slug,
                              @RequestBody Map<String, Object> map,
                              HttpSession session) {
        String title = "", text = "", recording = "";
        MultipartFile image = null, audio = null;
        if(map.containsKey("title")) title = map.get("title").toString();
        if(map.containsKey("text")) text = map.get("text").toString();
        if(map.containsKey("recording")) recording = map.get("recording").toString();
        if(map.containsKey("audio")) audio = (MultipartFile) map.get("audio");
        if(map.containsKey("image")) image = (MultipartFile) map.get("image");


        System.out.println("Contents of map");
        System.out.println(map);
        map.forEach((field, value)-> {
            System.out.println(field + ": " + value);
        });

        Sub sub = subService.getSubBySlug(slug);
        Post post = createPost(title, sub, text, image, audio, recording, session);
        try {
            postService.addNewPost(post);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(post);

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "/p/{slug}/{id}")
    public ResponseEntity replyPost(@PathVariable String slug,
                                    @PathVariable long id,
                                    Map<String, Object> map,
                                    HttpSession session) {
        Optional<Post> post = postService.getPostById(id);
        if(!post.isPresent()) return new ResponseEntity(HttpStatus.NOT_FOUND);

        Sub sub = subService.getSubBySlug(slug);
        String text = "", recording = "";
        MultipartFile image = null, audio = null;

        if(map.containsKey("text")) text = map.get("text").toString();
        if(map.containsKey("image")) image = (MultipartFile) map.get("image");
        if(map.containsKey("audio")) audio = (MultipartFile) map.get("audio");
        if(map.containsKey("recording")) recording = map.get("recording").toString();

        if(text.isEmpty() && image == null && audio == null && recording.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Reply reply = createReply(text, sub, image, audio, recording, session);
        try {
            replyService.addNewReply(reply);
            post.get().addReply(reply);
            postService.addNewPost(post.get());
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e);
        }
    }

    @PostMapping("/p/{slug}/{postId}/{id}")
    public ResponseEntity replyReply(@PathVariable String slug,
                                     @PathVariable long postId,
                                     @PathVariable long id,
                                     Map<String, Object> map,
                                     HttpSession session) {
        Optional<Reply> prevReply = replyService.getReplyById(id);
        if(!prevReply.isPresent()) return new ResponseEntity(HttpStatus.NOT_FOUND);

        Sub sub = subService.getSubBySlug(slug);
        String text = "", recording = "";
        MultipartFile image = null, audio = null;

        if(map.containsKey("text")) text = map.get("text").toString();
        if(map.containsKey("image")) image = (MultipartFile) map.get("image");
        if(map.containsKey("audio")) audio = (MultipartFile) map.get("audio");
        if(map.containsKey("recording")) recording = map.get("recording").toString();

        if(text.isEmpty() && image == null && audio == null && recording.isEmpty())
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        Reply reply = createReply(text, sub, image, audio, recording, session);
        try {
            replyService.addNewReply(reply);
            prevReply.get().addReply(reply);
            replyService.addNewReply(prevReply.get());
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e);
        }
    }




}