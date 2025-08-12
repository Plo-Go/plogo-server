package plogo.plogoserver.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import plogo.plogoserver.domain.RecommendCourse;

public interface RecommendRepository extends JpaRepository<RecommendCourse, Long> {
    List<RecommendCourse> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
