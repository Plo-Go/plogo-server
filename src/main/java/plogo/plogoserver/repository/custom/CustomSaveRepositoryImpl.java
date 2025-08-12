package plogo.plogoserver.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import plogo.plogoserver.domain.Course;

@Repository
@RequiredArgsConstructor
public class CustomSaveRepositoryImpl implements CustomSaveRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Course> findCoursesByUserId(Long userId) {
        return em.createQuery("SELECT sc.course FROM SaveCourse sc WHERE sc.user.id = :userId", Course.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
