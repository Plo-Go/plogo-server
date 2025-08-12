package plogo.plogoserver.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import plogo.plogoserver.converter.LogConverter;
import plogo.plogoserver.domain.Course;
import plogo.plogoserver.domain.Log;
import plogo.plogoserver.domain.LogPhoto;
import plogo.plogoserver.domain.User;
import plogo.plogoserver.repository.CourseRepository;
import plogo.plogoserver.repository.LogPhotoRepository;
import plogo.plogoserver.repository.LogRepository;
import plogo.plogoserver.repository.UserRepository;
import plogo.plogoserver.web.dto.response.LogResponseDto;

@Service
@RequiredArgsConstructor
public class LogService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final CourseRepository courseRepository;
    private final S3ImageService s3ImageService;
    private final LogPhotoRepository logPhotoRepository;


    @Transactional
    public List<LogResponseDto.LogDto> getCompletedList(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return LogConverter.toLogList(user);
    }

    @Transactional
    public LogResponseDto.LogDetailDto getLogDetail(String token, Long logId){
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Log log = logRepository.findLogById(logId);

        if (log == null) {
            throw new IllegalArgumentException("해당 로그 없음");
        }
        if(!log.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 로그 접근 불가");
        }
        return LogConverter.toLogDetail(log);
    }

    @Transactional
    public LogResponseDto.LogDetailDto updateLog(String token, Long logId, String logContent, List<String> existingUrls, List<MultipartFile> newImages) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 로그를 찾을 수 없습니다."));

        if(!log.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 로그 접근 불가");
        }

        List<LogPhoto> currentPhotos = log.getPhotos();
        List<LogPhoto> photosToKeep = new ArrayList<>();
        List<String> urlsToDelete = new ArrayList<>();

        if (existingUrls != null) {
            for(LogPhoto photo : currentPhotos){
                if(existingUrls.contains(photo.getUrl())){
                    photosToKeep.add(photo);
                } else {
                    urlsToDelete.add(photo.getUrl());
                }
            }
        } else {
            urlsToDelete.addAll(currentPhotos.stream().map(LogPhoto::getUrl).toList());
        }


        for(String url : urlsToDelete) {
            LogPhoto photoToDelete = currentPhotos.stream()
                    .filter(photo -> photo.getUrl().equals(url))
                    .findFirst()
                    .orElse(null);

            if (photoToDelete != null) {
                log.getPhotos().remove(photoToDelete);
                logPhotoRepository.delete(photoToDelete);
                s3ImageService.delete(url);
            }
        }


        log.setPhotos(photosToKeep);
        log.setLogContent(logContent);

        if(newImages != null) {
            for(MultipartFile image : newImages){
                String uploadUrl = s3ImageService.upload(image);
                photosToKeep.add(LogPhoto.builder().log(log).url(uploadUrl).build());
            }
        }


        logRepository.save(log);
        return LogConverter.toLogDetail(log);
    }

    //유저의 완주 여부 확인
    public Boolean getCompleteStatus(Long userId, Long CourseId) {
        Course targetCourse = courseRepository.findById(CourseId)
                .orElseThrow(() -> new RuntimeException("코스를 찾을 수 없습니다."));

        List<Long> idList = logRepository.findLogByUserId(userId).stream()
                .map(Log::getCourse)
                .map(Course::getId)
                .toList();

        return idList.contains(targetCourse.getId());
    }
}
