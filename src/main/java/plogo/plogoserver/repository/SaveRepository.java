package plogo.plogoserver.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import plogo.plogoserver.domain.SaveCourse;
import plogo.plogoserver.repository.custom.CustomSaveRepository;

public interface SaveRepository extends JpaRepository<SaveCourse, Long>, CustomSaveRepository {

    @Query("SELECT sc FROM SaveCourse sc WHERE sc.course.id = :courseId AND sc.user.id = :userId")
    Optional<SaveCourse> findByCourseIdAndUserId(@Param("courseId") Long courseId,@Param("userId") Long userId);

    List<SaveCourse> findByUserId(Long userId);
}
