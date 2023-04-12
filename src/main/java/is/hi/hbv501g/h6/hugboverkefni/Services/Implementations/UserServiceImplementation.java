package is.hi.hbv501g.h6.hugboverkefni.Services.Implementations;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.*;
import is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories.UserRepository;
import is.hi.hbv501g.h6.hugboverkefni.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;
    private UserDetailsServiceImplementation userDetailsService;
    @Autowired
    private Validator validator;
    private BCryptPasswordEncoder encoder;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, UserDetailsServiceImplementation userDetailsService) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        encoder = new BCryptPasswordEncoder();
    }

    /**
     * Returns all users in database
     *
     * @return List<User>
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns Default User "Anon" that enables
     * creating of posts and replies without being
     * logged in
     *
     * @return User
     */
    public User getAnon() {
        Optional<User> anon = userRepository.findByUsername("Anon");
        return anon.get();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserDetailsImplementation getAnonDetails() {
        return userDetailsService.loadUserByUsername(getAnon().getUsername());
    }

    /**
     * Returns User object containing email if it exists
     *
     * @param email String email of particular User
     * @return Optional<User>
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Returns User containing username if it exists
     *
     * @param userName String username of particular User
     * @return Optional<User>
     */
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Override
    public User getUserObjectByUserName(String username){
        return userRepository.findUserByUsername(username).get();
    }
    /**
     * Adds new User to the database if userName or email is not
     * already present in another User object in database
     * If either username or email is taken the BindingResult object
     * will be updated with rejectValue
     *
     * @param user   User object to be added to database
     * @param result BindingResult object
     */
    public void addNewUser(User user, BindingResult result) {
        Optional<User> username = userRepository.findByUsername(user.getUsername());
        Optional<User> email = userRepository.findByEmail(user.getEmail());

        user.setPassword(encoder.encode(user.getPassword()));

        if (username.isPresent()) result.rejectValue("username", "error.duplicate", "Username taken");
        if (email.isPresent()) result.rejectValue("email", "error.duplicate", "Email in use");

        if (!result.hasErrors()) userRepository.save(user);
    }

    public void addDefaultUser(User user) {
        Optional<User> userName = userRepository.findByUsername(user.getUsername());
        Optional<User> email = userRepository.findByEmail(user.getEmail());
        System.out.println("username: " + user.getUsername());
        System.out.println("user password: " + user.getPassword());
        user.setPassword(encoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    /**
     * Deletes User related to provided ID from database if it exists
     *
     * @param userId Long ID User identifier
     */
    public void deleteUser(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new IllegalStateException("user with id " + userId + " does not exist");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public void editUserName(User user) {
        Optional<User> usernameExists = userRepository.findByUsername(user.getUsername());
        if (usernameExists.isPresent()) throw new DuplicateKeyException("Username taken");
        userRepository.save(user);
    }

    @Override
    public User editRealName(User user) {
        return userRepository.save(user);
    }

    @Override
    public User editPassword(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void editEmail(User user) {
        Optional<User> userEmail = userRepository.findByEmail(user.getEmail());
        if (userEmail.isPresent()) throw new DuplicateKeyException("Email taken");
        userRepository.save(user);
    }

    @Override
    public User editAvatar(User user) {
        return userRepository.save(user);
    }

    /**
     * Checks if username provided in UI matches a User in database
     * If User exists, checks if password provided in UI matches password
     * in database
     * Returns User if password matches
     *
     * @param user User provided from UI
     * @return User
     */
    @Override
    public User loginUser(User user) {
        Optional<User> exists = getUserByUsername(user.getUsername());

        if (exists.isPresent()) {
            if (encoder.matches(user.getPassword(), exists.get().getPassword()))
                return exists.get();
        }
        return null;
    }

    @Override
    public User rinseUser(User user) {
        List<Post> posts = user.getPosts();
        List<Reply> replies = user.getReplies();

        for (Post post : posts) {
            User tempUser = post.getCreator();
            tempUser.setPosts(null);
            tempUser.setReplies(null);
            post.setCreator(tempUser);
            post.setReplies(null);
        }

        for (Reply reply : replies) {
            User tempUser = reply.getCreator();
            tempUser.setPosts(null);
            tempUser.setReplies(null);
            reply.setCreator(tempUser);
            reply.setReplies(null);
        }

        user.setPosts(posts);
        user.setReplies(replies);

        return user;
    }

    public User addSub(User user, Sub sub){
        List<Sub> subs = user.getSubs();
        subs.add(sub);
        user.setSubs(subs);
        return userRepository.save(user);
    }

    public User removeSub(User user, Sub sub){
        List<Sub> subs = user.getSubs();
        subs.remove(sub);
        user.setSubs(subs);
        return userRepository.save(user);
    }
}
