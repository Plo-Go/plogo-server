package plogo.plogoserver.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import plogo.plogoserver.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByEmail(String Email);
    Optional<User> findByAccessToken(String accessToken);
}
