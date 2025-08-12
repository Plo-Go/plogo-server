package plogo.plogoserver.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @Column(name = "course_id")
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<SaveCourse> saveCourseList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Log> logList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CheckCourse> checkCourseList = new ArrayList<>();

    private String image;
    private String name;
    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    private String phoneNum;
    private String phoneName;
    private int areaCode;
    private int sigunguCode;
    private String charge;
    private String address;
    private String homepage;
    private String homepageName;
    //저장된 수
    @Formula("(SELECT COUNT(sc.user_id) FROM save_course sc WHERE sc.course_id = course_id)")
    private int totalSaves;
}
