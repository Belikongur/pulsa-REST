package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    protected String changePostVote(long id, Boolean upvote, HttpSession session) {
        Post post = postService.getPostById(id).get();
        User user = (User) session.getAttribute("user");

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

        return "frontPage.html";
    }

    protected String changeReplyVote(long id, Boolean upvote, HttpSession session) {
        Reply reply = replyService.getReplyById(id).get();
        User user = (User) session.getAttribute("user");

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

        return "frontPage.html";
    }

    protected Post createPost(String title, Sub sub, String text, MultipartFile image, MultipartFile audio, String recording, User user) {
        Content content = createContent(text, image, audio, recording);

        if (user != null) return new Post(title, sub, content, user, new ArrayList<Voter>(), new ArrayList<Reply>());
        return new Post(title, sub, content, userService.getAnon(), new ArrayList<Voter>(), new ArrayList<Reply>());
    }


    protected Reply createReply(String text, Sub sub, MultipartFile image, MultipartFile audio, String recording, User user) {
        Content content = createContent(text, image, audio, recording);

        if (user != null) return new Reply(content, user, new ArrayList<Voter>(), new ArrayList<Reply>(), sub);

        return new Reply(content, userService.getAnon(), new ArrayList<Voter>(), new ArrayList<Reply>(), sub);
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

    @RequestMapping(value = "/p/{id}/vote", method = RequestMethod.GET)
    @ResponseBody
    protected String getPostVote(@PathVariable("id") long id, Model model) {
        Post post = postService.getPostById(id).get();
        return post.getVote().toString();
    }

    @RequestMapping(value = "/p/{id}/upvote", method = RequestMethod.POST)
    protected String upvotePost(@PathVariable("id") long id, HttpSession session) {

        return changePostVote(id, true, session);
    }

    @RequestMapping(value = "/p/{id}/upvote", method = RequestMethod.GET)
    protected String getUpvote(@PathVariable("id") long id, HttpSession session) {

        return changePostVote(id, true, session);
    }

    @RequestMapping(value = "/p/{id}/downvote", method = RequestMethod.POST)
    protected String downvotePost(@PathVariable("id") long id, HttpSession session) {
        return changePostVote(id, false, session);

    }

    @RequestMapping(value = "/p/{id}/downvote", method = RequestMethod.GET)
    protected String getDownvote(@PathVariable("id") long id, HttpSession session) {
        return changePostVote(id, false, session);

    }

    protected User getUser() {
        User user = userService.getUsers().get(0);
        return user;
    }

    protected Sub getSub() {
        Sub sub = subService.getSubs().get(0);
        return sub;
    }
}
