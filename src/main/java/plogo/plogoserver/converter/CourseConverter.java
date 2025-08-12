package plogo.plogoserver.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import plogo.plogoserver.domain.Course;
import plogo.plogoserver.service.SaveService;
import plogo.plogoserver.utils.CourseResponseData;
import plogo.plogoserver.web.dto.response.CourseDetailDTO;
import plogo.plogoserver.web.dto.response.CourseResponseDTO;

@Component
@RequiredArgsConstructor
public class CourseConverter {
    private final SaveService saveService;
    private final AreaConverter areaConverter;

    public CourseResponseDTO toResponseDTO(Course course, Boolean isSave) {
        String area = areaConverter.getAreaNameByCode(course.getAreaCode());

        return CourseResponseDTO.builder()
                .course_id(course.getId())
                .image(course.getImage())
                .area(course.getAddress())
                .name(course.getName())
                .isSave(isSave)
                .build();
    }

    public CourseDetailDTO toDetailDTO(Course course, Boolean isSave, Boolean isComplete) {

        return CourseDetailDTO.builder()
                .course_id(course.getId())
                .name(course.getName())
                .image(course.getImage())
                .summary(course.getContent())
                .address(course.getAddress())
                .tel(course.getPhoneNum())
                .telName(course.getPhoneName())
                .charge(course.getCharge())
                .homepage(course.getHomepage())
                .isSave(isSave)
                .isComplete(isComplete)
                .build();
    }

    public CourseResponseDTO jsonToResponse(CourseResponseData.Item item, Boolean isSave) {

        return CourseResponseDTO.builder()
                .course_id(Long.parseLong(item.getContentid()))
                .image(item.getMainimage())
                .area(item.getAddr())
                .name(item.getTitle())
                .isSave(isSave)
                .build();
        }
    }

