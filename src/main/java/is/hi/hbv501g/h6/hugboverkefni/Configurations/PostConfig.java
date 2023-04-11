package is.hi.hbv501g.h6.hugboverkefni.Configurations;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.PostRepository;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.ReplyRepository;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.RoleRepository;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.SubRepository;
import is.hi.hbv501g.h6.hugboverkefni.Services.Implementations.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class PostConfig {
    @Autowired
    RoleRepository roleRepository;

    @Bean
    CommandLineRunner commandLineRunner(PostRepository postRepository, UserServiceImplementation userService, ReplyRepository replyRepository, SubRepository subRepository) {
        return args -> {
            User anon = new User("Anon",
                    "anon",
                    "Anonymous",
                    "https://res.cloudinary.com/dc6h0nrwk/image/upload/v1666386282/xov6nkbsxf3hmhuqb3jn.png",
                    "anon@anon.com");
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            Role userRole = new Role(ERole.ROLE_USER);
            Role anonRole = new Role(ERole.ROLE_ANON);
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
            roleRepository.save(anonRole);
            Set<Role> anonRoles = new HashSet<>();
            anonRoles.add(anonRole);
            anon.setRoles(anonRoles);
            userService.addDefaultUser(anon);
            Sub sub = new Sub("Háskólalífið");
            sub.setImage("https://res.cloudinary.com/dc6h0nrwk/image/upload/v1665799070/i3g9v3wdjlzbeaxvhihc.jpg");
            subRepository.save(sub);
            LocalDateTime date = LocalDateTime.now();
            Content content = new Content("Hallo Heimur", "http://res.cloudinary.com/dc6h0nrwk/image/upload/v1664770603/q1h9kuthpcgqpkgz8c8r.png", "http://res.cloudinary.com/dc6h0nrwk/video/upload/v1664772263/ltaf63f4ococlisbqtfo.mp3", "http://res.cloudinary.com/dc6h0nrwk/video/upload/v1664826994/m11awfhrnvyfw6uc5crg.ogg");
            User user = new User(
                    "gervinotandi1",
                    "gervi",
                    "gervimadur",
                    "https://cdn2.iconfinder.com/data/icons/food-drink-60/50/1F32D-hot-dog-1024.png",
                    "net@fi.ng");
            Content content2 = new Content("Sugoi!", "http://res.cloudinary.com/dc6h0nrwk/image/upload/v1664806729/uakzskirbbkgb30tejb0.gif", "http://res.cloudinary.com/dc6h0nrwk/video/upload/v1664806897/xzn8cw8bkvv3moc3d8ua.mp3", "");
            User user2 = new User(
                    "gervinotandi3",
                    "gervi",
                    "gervikona",
                    "https://cdn2.iconfinder.com/data/icons/food-drink-60/50/1F32D-hot-dog-1024.png",
                    "net@fe.ng");
            User user3 = new User(
                    "test",
                    "test",
                    "test",
                    "https://cdn2.iconfinder.com/data/icons/food-drink-60/50/1F32D-hot-dog-1024.png",
                    "test@test.com");
            Reply reply = new Reply(content2, user2, new ArrayList<Voter>(), new ArrayList<Reply>(), sub);
            Voter voter = new Voter(user, true);
            Set<Role> user2Roles = new HashSet<>();
            user2Roles.add(userRole);
            user2.setRoles(user2Roles);
            userService.addDefaultUser(user2);
            replyRepository.save(reply);
            List<Reply> replies = new ArrayList<Reply>();
            replies.add(0, reply);
            Post Jonathan = new Post(
                    "Test titill",
                    sub,
                    content,
                    user,
                    new ArrayList<Voter>(),
                    replies
            );

            Content content3 = new Content("C3P0 and R2D2 walk into a bar...", "http://res.cloudinary.com/dc6h0nrwk/image/upload/v1666562503/t94r47hzji1i2aoyxhx7.jpg", "http://res.cloudinary.com/dc6h0nrwk/video/upload/v1666563900/rzqrqkiggilk4wbbztqu.mp3", "");
            Sub starwars = new Sub("Star Wars");
            starwars.setImage("https://cdn.shopify.com/s/files/1/1057/4964/t/24/assets/star-wars-banner.jpeg");
            subRepository.save(starwars);
            Post Joseph = new Post("C3P0 and R2D2 walk into a bar...",
                    starwars,
                    content3,
                    anon,
                    new ArrayList<Voter>(),
                    new ArrayList<Reply>());

            Set<Role> user1Roles = new HashSet<>();
            user1Roles.add(userRole);
            user.setRoles(user1Roles);

            Set<Role> user3Roles = new HashSet<>();
            user3Roles.add(userRole);
            user3.setRoles(user3Roles);
            userService.addDefaultUser(user);
            userService.addDefaultUser(user3);
            postRepository.save(Jonathan);
            postRepository.save(Joseph);
        };
    }
}
