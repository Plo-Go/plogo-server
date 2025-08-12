package plogo.plogoserver.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import plogo.plogoserver.domain.Log;
import plogo.plogoserver.domain.User;

public interface LogRepository extends JpaRepository<Log, Long> {
    Log findLogById(Long id);

    List<Log> findLogByUserId(Long userId);

    void deleteByUser(User user);


}
