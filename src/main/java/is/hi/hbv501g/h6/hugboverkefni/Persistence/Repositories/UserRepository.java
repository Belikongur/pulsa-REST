package is.hi.hbv501g.h6.hugboverkefni.Persistence.Repositories;

import is.hi.hbv501g.h6.hugboverkefni.Persistence.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    Optional<User> findUserByUsername(String username);

}


