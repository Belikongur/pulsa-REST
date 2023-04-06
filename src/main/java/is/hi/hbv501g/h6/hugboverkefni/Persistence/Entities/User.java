package is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Fetch;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(name = "users")
@Embeddable
public class User {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long user_id;
    @NotBlank(message = "Field must not be empty")
    @Size(min = 3, message = "{Size.name}")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Field must not be empty")
    @Size(min = 2, message = "{Size.pass}")
    private String password;
    @NotBlank(message = "Field must not be empty")
    @Size(min = 3, message = "{Size.name}")
    private String realName;
    private String avatar;
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable( name="user_roles",
                joinColumns = @JoinColumn(name="user_id"),
                inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Sub> subs = new ArrayList<Sub>();

    @OneToMany()
    @JoinColumn(name="user_id")
    @JsonManagedReference
    private List<Post> posts = new ArrayList<Post>();

    @OneToMany
    @JoinColumn(name = "user_id")
    private List<Reply> replies = new ArrayList<Reply>();
    private LocalDateTime created;
    private LocalDateTime updated;

    public User() {
    }

    /**
     * User entity
     *
     * @param username String Unique user identifier
     * @param password String Top secret password
     * @param realName String Users real name
     * @param avatar   String DataURL of uploaded image
     *                 representing the user
     * @param email    String email for user
     * @return User
     */
    public User(String username,
                String password,
                String realName,
                String avatar,
                String email) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.avatar = avatar;
        this.email = email;
        this.setCreated();
        this.setUpdated();
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long id) {
        this.user_id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return created.format(formatter);
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setCreated() {
        this.created =  LocalDateTime.now();
    }

    public List<Sub> getSubs() {
        return subs;
    }

    public void setSubs(List<Sub> subs) {
        this.subs = subs;
    }

    public void addSub(Sub sub) { this.subs.add(sub); }

    public boolean isFollowing(Sub sub) { return this.subs.contains(sub); }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setUpdated() {
        this.updated = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
