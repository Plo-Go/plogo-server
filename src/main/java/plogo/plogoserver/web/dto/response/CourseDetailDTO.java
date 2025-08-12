package plogo.plogoserver.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDetailDTO {
    private Long course_id;
    private String name;
    private String image;
    private String summary;
    private String address;
    private String tel;
    private String telName;
    private String charge;
    private String homepage;
    private Boolean isSave;
    private Boolean isComplete;
}
