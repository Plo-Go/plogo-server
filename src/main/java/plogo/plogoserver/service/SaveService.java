package plogo.plogoserver.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.Course;
import plogo.plogoserver.repository.CourseRepository;
import plogo.plogoserver.repository.SaveRepository;

@Service
@RequiredArgsConstructor
public class SaveService {
    private final CourseRepository courseRepository;
    private final SaveRepository saveRepository;

    //코스의 저장여부 판단
    public Boolean getSaveStatus(Long courseId, Long userId) {
        Course targetCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("코스를 찾을 수 없습니다."));

        List<Long> idList = saveRepository.findCoursesByUserId(userId).stream()
                .map(Course::getId)
                .toList();

        return idList.contains(targetCourse.getId());
    }
}
