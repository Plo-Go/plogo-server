package plogo.plogoserver.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.Sigungu;
import plogo.plogoserver.repository.SigunguRepository;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final CourseService courseService;
    private final SigunguRepository sigunguRepository;

    public void runSaveAll() {
        List<Sigungu> sigungus = sigunguRepository.findAll();
        for (Sigungu sigungu : sigungus) {
            courseService.saveSigunguCourses(sigungu.getAreaCode(), sigungu.getSigunguCode());
        }
    }
}
