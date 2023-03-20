package is.hi.hbv501g.h6.hugboverkefni.Controllers;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Services.CloudinaryService;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Optional;


@Controller
public class PostController extends BaseController {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    @Autowired
    public PostController(PostServiceImplementation postService,
                          UserServiceImplementation userService,
                          ReplyServiceImplementation replyService,
                          VoteServiceImplementation voteService,
                          SubServiceImplementation subService,
                          CloudinaryService cloudinaryService) {
        super(postService, userService, replyService, voteService, subService, cloudinaryService);
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    @RequestMapping(value = "/p/{slug}/{id}", method = RequestMethod.GET)
    public String postPage(@PathVariable("slug") String slug, @PathVariable("id") long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (!post.isPresent()) return "postNotFound";

        model.addAttribute("post", post.get());
        model.addAttribute("postReplies", post.get().getReplies());
        model.addAttribute("reply", new Reply());
        model.addAttribute("content", new Content());
        model.addAttribute("sub", subService.getSubBySlug(slug));
        return "postPage";
    }

    @RequestMapping(value = "/p/{slug}/newPost", method = RequestMethod.GET)
    public String newPostGET(@PathVariable String slug, Post post, Model model) {
        model.addAttribute("slug", slug);
        return "newPost";
    }

    @RequestMapping(value = "/p/{slug}/newPost", method = RequestMethod.POST)
    public String newPostPOST(@PathVariable String slug,
                              String title,
                              String text,
                              @RequestParam("image") MultipartFile image,
                              @RequestParam("audio") MultipartFile audio,
                              @RequestParam("recording") String recording,
                              Model model,
                              HttpSession session) {
        Sub sub = subService.getSubBySlug(slug);
        String renderedText = htmlRenderer.render(markdownParser.parse(text));
        Post newPost = createPost(title, sub, renderedText, image, audio, recording, session);
        postService.addNewPost(newPost);
        return "redirect:/p/" + slug + '/' + newPost.getPostId();
    }

    @RequestMapping(value = "/p/{slug}/{id}", method = RequestMethod.POST)
    public String replyPost(@PathVariable String slug,
                            @PathVariable("id") long id,
                            String text,
                            @RequestParam("image") MultipartFile image,
                            @RequestParam("audio") MultipartFile audio,
                            @RequestParam("recording") String recording,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        Optional<Post> post = postService.getPostById(id);
        if (!post.isPresent()) return "postNotFound";
        if(text.isEmpty() && image.isEmpty() && audio.isEmpty() && recording.equals("recording")) {
            redirectAttributes.addFlashAttribute("emptyPostReply", true);
            return "redirect:/p/" + slug + '/' + post.get().getPostId();
        }
        Sub sub = subService.getSubBySlug(slug);
        String renderedText = htmlRenderer.render(markdownParser.parse(text));
        Reply reply = createReply(renderedText, sub, image, audio, recording, session);
        replyService.addNewReply(reply);
        post.get().addReply(reply);
        postService.addNewPost(post.get());

        return "redirect:/p/" + slug + '/' + post.get().getPostId();
    }


    @RequestMapping(value = "/p/{slug}/{postId}/{id}", method = RequestMethod.POST)
    public String replyReply(@PathVariable String slug,
                             @PathVariable("postId") long postId,
                             @PathVariable("id") long id,
                             String text,
                             @RequestParam("image") MultipartFile image,
                             @RequestParam("audio") MultipartFile audio,
                             @RequestParam("recording") String recording,
                             Model model, RedirectAttributes redirectAttributes,
                             HttpSession session) {
        Optional<Reply> prevReply = replyService.getReplyById(id);
        if (!prevReply.isPresent()) return "postNotFound";
        if(text.isEmpty() && image.isEmpty() && audio.isEmpty() && recording.equals("recording")) {
            redirectAttributes.addFlashAttribute("emptyReplyReply", id);
            return "redirect:/p/" + slug + '/' + postId;
        }
        Sub sub = subService.getSubBySlug(slug);
        String renderedText = htmlRenderer.render(markdownParser.parse(text));
        Reply reply = createReply(renderedText, sub, image, audio, recording, session);
        replyService.addNewReply(reply);
        prevReply.get().addReply(reply);
        replyService.addNewReply(prevReply.get());

        return "redirect:/p/" + slug + '/' + postId;
    }

}
