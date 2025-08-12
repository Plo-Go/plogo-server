package plogo.plogoserver.repository.custom;

import java.util.List;
import plogo.plogoserver.domain.Course;

public interface CustomSaveRepository {

    List<Course> findCoursesByUserId(Long userId);
}
