package plogo.plogoserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
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
import plogo.plogoserver.converter.AreaConverter;
import plogo.plogoserver.converter.CourseConverter;
import plogo.plogoserver.converter.NaverBlogConverter;
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
import plogo.plogoserver.utils.CourseResponseData;
import plogo.plogoserver.utils.NaverBlog;
import plogo.plogoserver.utils.RecommendSystem;
import plogo.plogoserver.utils.TestResponse;
import plogo.plogoserver.utils.TourApi;
import plogo.plogoserver.utils.UserHelper;
import plogo.plogoserver.web.dto.response.CourseDetailDTO;
import plogo.plogoserver.web.dto.response.CourseResponseDTO;
import plogo.plogoserver.web.dto.response.NaverBlogDTO;
import plogo.plogoserver.web.dto.response.PreferenceRequestBody;
import plogo.plogoserver.web.dto.response.SaveStatusDTO;

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
    private final UserHelper userHelper;

    private final ObjectMapper objectMapper;

    //지역기반으로 코스를 받아 옴
    public List<CourseResponseDTO> getByAreaName(Long areaCode) {
        User user = userHelper.getAuthenticatedUser();

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
    public CourseDetailDTO getCourseDetail(Long courseId) {
        User user = userHelper.getAuthenticatedUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("코스를 찾을 수 없습니다."));
        Boolean isSave = saveService.getSaveStatus(course.getId(), user.getId());
        Boolean isComplete = logService.getCompleteStatus(user.getId(), course.getId());

        saveRecentCourse(user, courseId);

        return courseConverter.toDetailDTO(course, isSave, isComplete);
    }

    //클릭 때 마다 코스를 저장하고 취소하는 토글 기능
    public SaveStatusDTO toggleSaveCourse(Long courseId) {
        User user = userHelper.getAuthenticatedUser();
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
    public Long completeCourse(Long courseId) {
        User user = userHelper.getAuthenticatedUser();
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
    public List<CourseResponseDTO> analyzePreference(PreferenceRequestBody request) {
        User user = userHelper.getAuthenticatedUser();

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
    public List<CourseResponseDTO> getRecommendCourse() {
        User user = userHelper.getAuthenticatedUser();

        return recommendRepository.findByUserId(user.getId()).stream()
                .map(RecommendCourse::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .collect(Collectors.toList());
    }

    //저장한 코스 반환
    public List<CourseResponseDTO> getSaveCourse() {
        User user = userHelper.getAuthenticatedUser();

        return saveRepository.findByUserId(user.getId()).stream()
                .map(SaveCourse::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .collect(Collectors.toList());
    }

    //완주한 코스 반환
    public List<CourseResponseDTO> getCompleteCourse() {
        User user = userHelper.getAuthenticatedUser();

        return logRepository.findLogByUserId(user.getId()).stream()
                .map(Log::getCourse)
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //최근 핫한 코스
    public List<CourseResponseDTO> getHotCourses() {
        User user = userHelper.getAuthenticatedUser();

        return courseRepository.findCourseOrderByImage(10).stream()
                .map(course -> courseConverter
                        .toResponseDTO(course, saveService.getSaveStatus(course.getId(), user.getId())))
                .toList();
    }

    //코스 검색 기능
    public List<CourseResponseDTO> getCourseByKeyword(String keyword) {
        User user = userHelper.getAuthenticatedUser();

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

    // 인증 필요 없는 저장용 메서드
    //@Transactional
    public void saveSigunguCourses(int areaCode, int sigunguCode) {
        int pageNo = 1;
        int totalCount = 0;

        do {
            String jsonResponse = tourApi.getSigunguCourse(areaCode, sigunguCode, pageNo);

            // JSON 응답 여부 확인
            if (jsonResponse == null || !jsonResponse.trim().startsWith("{")) {
                System.err.println("⚠️ JSON 아님, 원본 응답 출력 → " + jsonResponse);
                break; // return ❌ → break (현재 시군구만 건너뛰고 다음 진행)
            }

            try {
                TestResponse testResponse = objectMapper.readValue(jsonResponse, TestResponse.class);

                if (testResponse.getResponse() == null
                        || testResponse.getResponse().getBody() == null
                        || testResponse.getResponse().getBody().getItems() == null
                        || testResponse.getResponse().getBody().getItems().getItem() == null) {
                    System.out.println("⚠️ No items found → areaCode=" + areaCode + ", sigunguCode=" + sigunguCode + ", page=" + pageNo);
                    break; // return ❌ → break (현재 시군구 끝내고 다음 시군구로)
                }

                List<TestResponse.Item> items = testResponse.getResponse().getBody().getItems().getItem();
                totalCount = testResponse.getResponse().getBody().getTotalCount();

                for (TestResponse.Item item : items) {
                    Long courseId = Long.valueOf(item.getContentid());

                    if (courseRepository.existsById(courseId)) {
                        System.out.println("⚠️ Already exists, skip: " + courseId);
                        continue;
                    }

                    Course course = Course.builder()
                            .id(courseId)
                            .image(item.getMainimage())
                            .address(item.getAddr())
                            .phoneNum(item.getTel())
                            .phoneName(item.getTelname())
                            .name(item.getTitle())
                            .content(item.getSummary())
                            .areaCode(Integer.parseInt(item.getAreacode()))
                            .sigunguCode(Integer.parseInt(item.getSigungucode()))
                            .charge(item.getCharge())
                            .build();

                    courseRepository.save(course);
                    //courseRepository.flush();
                    System.out.println("✅ Course saved: " + course.getName());
                }

                pageNo++;

            } catch (Exception e) {
                System.err.println("❌ 파싱 실패 → areaCode=" + areaCode + ", sigunguCode=" + sigunguCode + ", page=" + pageNo);
                e.printStackTrace();
                break; // return ❌ → break (현재 시군구만 중단)
            }

        } while (pageNo * 40 < totalCount); // numOfRows=40 기준
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
    public List<CourseResponseDTO> getRecentCourse() {
        User user = userHelper.getAuthenticatedUser();

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
    public List<CourseResponseDTO> getSigunguCourse(Long sigunguId) {
        User user = userHelper.getAuthenticatedUser();

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
