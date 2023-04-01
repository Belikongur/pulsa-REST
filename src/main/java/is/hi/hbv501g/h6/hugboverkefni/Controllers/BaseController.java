package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public abstract class BaseController {
    protected final PostServiceImplementation postService;
    protected final UserServiceImplementation userService;
    protected final ReplyServiceImplementation replyService;
    protected final SubServiceImplementation subService;
    protected final CloudinaryService cloudinaryService;
    protected final VoteServiceImplementation voteService;

    @Autowired
    public BaseController(PostServiceImplementation postService,
                          UserServiceImplementation userService,
                          ReplyServiceImplementation replyService,
                          VoteServiceImplementation voteService,
                          SubServiceImplementation subService,
                          CloudinaryService cloudinaryService) {
        this.postService = postService;
        this.userService = userService;
        this.replyService = replyService;
        this.voteService = voteService;
        this.subService = subService;
        this.cloudinaryService = cloudinaryService;
    }

    protected Content createContent(String text, MultipartFile image, MultipartFile audio, String recording) {
        String imgUrl = "";
        String audioUrl = "";
        String recordingUrl = "";
        if (image != null && !image.isEmpty()) imgUrl = cloudinaryService.securify(cloudinaryService.uploadImage(image));
        if (audio != null && !audio.isEmpty()) audioUrl = cloudinaryService.securify(cloudinaryService.uploadAudio(audio));
        if (recording != null && recording.length() != 9) recordingUrl = cloudinaryService.securify(cloudinaryService.uploadRecording(recording));
        Content c = new Content(text, imgUrl, audioUrl, recordingUrl);
        return c;
    }

    protected Post createPost(String title, Sub sub, String text, MultipartFile image, MultipartFile audio, String recording, User user) {
        Content content = createContent(text, image, audio, recording);

        if (user != null) return new Post(title, sub, content, user, new ArrayList<Voter>(), new ArrayList<Reply>());
        return new Post(title, sub, content, userService.getAnon(), new ArrayList<Voter>(), new ArrayList<Reply>());
    }

    protected @ResponseBody ResponseEntity changePostVote(long id, Boolean upvote) {
        Post post = postService.getPostById(id).get();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserObjectByUserName(userDetails.getUsername());
        Optional<Voter> voter = post.findVoter(user);

        if (voter.isEmpty()) {
            Voter newVoter = new Voter(user, upvote);
            post.addVote(newVoter);
            voteService.addVoter(newVoter);
        } else if (voter.get().isVote() != upvote) {
            voter.get().setVote(upvote);
        } else {
            post.removeVote(voter.get());
            voteService.removeVoter(voter.get());
        }

        postService.addNewPost(post);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(post);
    }

    @RequestMapping(value = "p/{id}/vote", method = RequestMethod.GET)
    @ResponseBody
    protected String getPostVote(@PathVariable("id") long id, Model model) {
        Post post = postService.getPostById(id).get();
        return post.getVote().toString();
    }

    @RequestMapping(value = "p/{id}/upvote", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    protected @ResponseBody ResponseEntity upvotePost(@PathVariable("id") long id) {
        return changePostVote(id, true);
    }

    @RequestMapping(value = "p/{id}/upvote", method = RequestMethod.GET)
    protected @ResponseBody ResponseEntity getUpvote(@PathVariable("id") long id) {

        return changePostVote(id, true);
    }

    @RequestMapping(value = "p/{id}/downvote", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    protected @ResponseBody ResponseEntity downvotePost(@PathVariable("id") long id) {
        return changePostVote(id, false);

    }

    @RequestMapping(value = "p/{id}/downvote", method = RequestMethod.GET)
    protected @ResponseBody ResponseEntity getDownvote(@PathVariable("id") long id) {
        return changePostVote(id, false);

    }

    protected Reply createReply(String text, Sub sub, MultipartFile image, MultipartFile audio, String recording, User user) {
        Content content = createContent(text, image, audio, recording);

        if (user != null) return new Reply(content, user, new ArrayList<Voter>(), new ArrayList<Reply>(), sub);

        return new Reply(content, userService.getAnon(), new ArrayList<Voter>(), new ArrayList<Reply>(), sub);
    }

    protected @ResponseBody ResponseEntity changeReplyVote(long id, Boolean upvote) {
        Reply reply = replyService.getReplyById(id).get();
        UserDetailsImplementation userDetails = (UserDetailsImplementation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserObjectByUserName(userDetails.getUsername());

        Optional<Voter> voter = reply.findVoter(user);

        if (voter.isEmpty()) {
            Voter newVoter = new Voter(user, upvote);
            reply.addVote(newVoter);
            voteService.addVoter(newVoter);
        } else if (voter.get().isVote() != upvote) {
            voter.get().setVote(upvote);
        } else {
            reply.removeVote(voter.get());
            voteService.removeVoter(voter.get());
        }

        replyService.addNewReply(reply);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reply);
    }

    @RequestMapping(value = "/r/{id}/vote", method = RequestMethod.GET)
    @ResponseBody
    public String getReplyVote(@PathVariable("id") long id) {
        Reply reply = replyService.getReplyById(id).get();
        return reply.getVote().toString();
    }

    @RequestMapping(value = "/r/{id}/upvote", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ResponseEntity upvoteReply(@PathVariable("id") long id) {
        return changeReplyVote(id, true);
    }

    @RequestMapping(value = "/r/{id}/downvote", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ResponseEntity downvoteReply(@PathVariable("id") long id) {
        return changeReplyVote(id, false);
    }

    @RequestMapping(value = "/r/{id}/upvote", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getUpvoteReply(@PathVariable("id") long id) {
        return changeReplyVote(id, true);
    }

    @RequestMapping(value = "/r/{id}/downvote", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity getDownvoteReply(@PathVariable("id") long id) {
        return changeReplyVote(id, false);
    }

}
