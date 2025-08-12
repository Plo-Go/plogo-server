package plogo.plogoserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.Course;
import plogo.plogoserver.domain.Log;
import plogo.plogoserver.domain.RecommendCourse;
import plogo.plogoserver.domain.SaveCourse;
import plogo.plogoserver.domain.Sigungu;
import plogo.plogoserver.domain.User;
import plogo.plogoserver.repository.CourseRepository;
import plogo.plogoserver.repository.LogRepository;
import plogo.plogoserver.repository.RecommendRepository;
import plogo.plogoserver.repository.SaveRepository;
import plogo.plogoserver.repository.SigunguRepository;
import plogo.plogoserver.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseConverter courseConverter;
    private final AreaConverter areaConverter;
    private final SaveRepository saveRepository;
    private final UserRepository userRepository;
    private final NaverBlog naverBlog;
    private final NaverBlogConverter naverBlogConverter;
    private final LogRepository logRepository;
    private final RecommendSystem recommendSystem;
    private final SaveService saveService;
    private final RecommendRepository recommendRepository;
    private final LogService logService;
    private final RedisTemplate<String, Long> redisTemplate;
    private final SearchService searchService;
    private final TourApi tourApi;
    private final SigunguRepository sigunguRepository;

    //지역기반으로 코스를 받아 옴
    public List<CourseResponseDTO> getByAreaName(String token, Long areaCode) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        //지역 전체를 뜻하는 0번을 입력할 경우 전체 코스를 가져옴
        if (areaCode == 0) {
            return courseRepository.findAllOrderByTotalSaves(182).stream()
                    .map(course -> courseConverter
                            .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                    .toList();
        }
        //특정 지역 코드를 입력한 경우
        else {
            return courseRepository.findByAreaCode(areaCode).stream()
                    .map(course -> courseConverter
                            .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                    .collect(Collectors.toList());
        }

    }

    //클릭 시 상세 조회
    public CourseDetailDTO getCourseDetail(String token, Long courseId) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));
        Boolean isSave = saveService.getSaveStatus(course.getId(), user.getId());
        Boolean isComplete = logService.getCompleteStatus(user.getId(), course.getId());

        saveRecentCourse(user, courseId);

        return courseConverter.toDetailDTO(course, isSave, isComplete);
    }

    //클릭 때 마다 코스를 저장하고 취소하는 토글 기능
    public SaveStatusDTO toggleSaveCourse(String token, Long courseId) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));
        Optional<SaveCourse> saveCourse = saveRepository.findByCourseIdAndUserId(courseId, user.getId());

        //존재할 경우 테이블에서 삭제
        if (saveCourse.isPresent()) {
            saveRepository.delete(saveCourse.get());
        } else { //존재하지 않을 경우 추가
            SaveCourse addSaveCourse = new SaveCourse(null, course, user);
            saveRepository.save(addSaveCourse);
        }

        return SaveStatusDTO.builder()
                .isSave(saveService.getSaveStatus(courseId, user.getId()))
                .build();
    }

    //네이버 블로그 포스팅 가져오기
    public List<NaverBlogDTO> getNaverBlogs(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));

        String courseName = course.getName();
        List<NaverBlogDTO> postDTOs = new ArrayList<>();

        String naverString = naverBlog.search(courseName);
        JSONObject jsonObject = new JSONObject(naverString);
        JSONArray jsonArray = jsonObject.getJSONArray("items");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject JSONPost = (JSONObject) jsonArray.get(i);
            NaverBlogDTO postDTO = naverBlogConverter.JSONToDTO(JSONPost);

            postDTOs.add(postDTO);
        }

        return postDTOs;
    }

    //코스 완주 할 경우 저장
    public Long completeCourse(String token, Long courseId) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));

        Log log = Log.builder()
                .course(course)
                .user(user)
                .build();
        logRepository.save(log);

        long currentStampCount = user.getStampCount() != null ? user.getStampCount() : 0;
        long updatedStampCount = currentStampCount + 1;

        user.setStampCount(updatedStampCount);
        userRepository.save(user);

        return log.getId();

    }

    //플라스크 서버로 분석요청 보낸 후 코스id 받아옴
    public List<CourseResponseDTO> analyzePreference(String token, PreferenceRequestBody request) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        //기존에 저장되어 있는 코스가 있으면 삭제
        recommendRepository.deleteByUserId(user.getId());

        //플라스크 서버에서 코스 분석 후 반환
        List<Long> courseIds = recommendSystem.getRecommendations(request);

        List<Course> courses = courseIds.stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        //서버에 저장
        saveCourses(courses, user);

        return courses.stream()
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .collect(Collectors.toList());
    }

    //플라스크 서버에서 받아온 코스 저장
    public void saveCourses(List<Course> courses, User user) {
        for(Course course : courses) {
            RecommendCourse recommendCourse = new RecommendCourse(null, course, user);
            recommendRepository.save(recommendCourse);
        }
    }

    //디비에 있는 있는 추천 코스 반환
    public List<CourseResponseDTO> getRecommendCourse(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return recommendRepository.findByUserId(user.getId()).stream()
                .map(RecommendCourse::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .collect(Collectors.toList());
    }

    //저장한 코스 반환
    public List<CourseResponseDTO> getSaveCourse(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return saveRepository.findByUserId(user.getId()).stream()
                .map(SaveCourse::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .collect(Collectors.toList());
    }

    //완주한 코스 반환
    public List<CourseResponseDTO> getCompleteCourse(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return logRepository.findLogByUserId(user.getId()).stream()
                .map(Log::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //최근 핫한 코스
    public List<CourseResponseDTO> getHotCourses(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return courseRepository.findCourseOrderByImage(10).stream()
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //코스 검색 기능
    public List<CourseResponseDTO> getCourseByKeyword(String token, String keyword) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        searchService.saveRecentSearchLog(user, keyword);

        //검색 결과의 최대 개수
        int totalCount = 20;
        //코스를 이름 기준으로 검색
        List<Long> coursesByName = courseRepository.findByNameKeyword(keyword, totalCount).stream()
                .map(Course::getId)
                .toList();

        totalCount -= coursesByName.size();

        //주소 기준으로 검색
        List<Long> coursesByAddress = courseRepository.findByAddressKeyword(keyword, totalCount).stream()
                .map(Course::getId)
                .toList();

        totalCount -= coursesByAddress.size();

        //내용 기준으로 검색
        List<Long> coursesByContent = courseRepository.findByContentKeyword(keyword, totalCount).stream()
                .map(Course::getId)
                .toList();

        //세 가지 기준으로 검색한 코스 결과 합치기 : Set으로 중복제거
        Set<Long> combinedSet = new HashSet<>();
        combinedSet.addAll(coursesByName);
        combinedSet.addAll(coursesByAddress);
        combinedSet.addAll(coursesByContent);

        //리스트로 변경 후 코스로 형변환
        List<Course> combinedList = new ArrayList<>(combinedSet).stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        //반환
        return combinedList.stream()
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //최근 확인한 코스 데이터 저장
    public void saveRecentCourse(User user, Long courseId) {

        //key값 : Course + 유저 id 값
        String key = "Course" + user.getId();

        redisTemplate.opsForList().remove(key, 1, courseId);

        Long size = redisTemplate.opsForList().size(key);

        //10개를 넘을 경우 가장 오래된 데이터 삭제
        if(size == 10) {
            redisTemplate.opsForList().rightPop(key);
        }

        redisTemplate.opsForList().leftPush(key, courseId);
    }

    //최근 확인한 코스 데이터 조회
    public List<CourseResponseDTO> getRecentCourse(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String key = "Course" + user.getId();

        List<Course> courses = redisTemplate.opsForList().range(key, 0, 10).stream()
                .map(courseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return courses.stream()
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //시군구별 코스 조회
    public List<CourseResponseDTO> getSigunguCourse(String token, Long sigunguId) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Sigungu sigungu = sigunguRepository.findById(sigunguId)
                .orElseThrow(() -> new IllegalArgumentException("시군구를 찾을 수 없습니다."));

        int areaCode = sigungu.getAreaCode();
        int sigunguCode = sigungu.getSigunguCode();

        String jsonResponse = tourApi.getSigunguCourse(areaCode, sigunguCode);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            CourseResponseData courseResponseData = objectMapper.readValue(jsonResponse, CourseResponseData.class);
            List<CourseResponseData.Item> items = courseResponseData.getResponse().getBody().getItems().getItem();

            return items.stream()
                    .map(item -> courseConverter
                            .jsonToResponse(item,
                                    saveService.getSaveStatus(Long.parseLong(item.getContentid()), user.getId())))
                    .toList();

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // JSON 처리 중 발생하는 예외 (파싱 문제 등)
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
