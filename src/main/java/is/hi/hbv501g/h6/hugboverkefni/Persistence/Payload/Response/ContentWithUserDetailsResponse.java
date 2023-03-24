package is.hi.hbv501g.h6.hugboverkefni.Persistence.Payload.Response;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.Content;
import org.springframework.security.core.userdetails.UserDetails;

public class ContentWithUserDetailsResponse {
    private Content content;
    private UserDetails userDetails;

    public ContentWithUserDetailsResponse(Content content, UserDetails userDetails) {
        this.content = content;
        this.userDetails = userDetails;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}
