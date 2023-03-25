package usw.suwiki.domain.lecture.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usw.suwiki.domain.evaluation.EvaluatePostsToLecture;
import usw.suwiki.domain.lecture.LectureFindOption;
import usw.suwiki.domain.lecture.LectureToJsonArray;
import usw.suwiki.domain.lecture.dto.LectureDetailResponseDto;
import usw.suwiki.domain.lecture.dto.LectureListAndCountDto;
import usw.suwiki.domain.lecture.dto.LectureResponseDto;
import usw.suwiki.domain.lecture.entity.Lecture;
import usw.suwiki.domain.lecture.repository.LectureRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LectureService {

    private final LectureRepository lectureRepository;

    @Transactional(readOnly = true)
    public LectureToJsonArray searchLecture(String searchValue, Optional<String> option,
        Optional<Integer> page, Optional<String> majorType) {
        LectureFindOption findOption = createLectureFindOption(option, page, majorType);

        if (findOption.checkMajorTypeEmpty()) {
            return findLectureByFindOption(searchValue, findOption);
        }
        return findLectureByMajorType(searchValue, findOption);
    }

    public LectureToJsonArray findAllLecture(Optional<String> option,
        Optional<Integer> page, Optional<String> majorType) {
        LectureFindOption findOption = createLectureFindOption(option, page, majorType);
        if (findOption.checkMajorTypeEmpty()) {
            return findAllLectureByFindOption(findOption);
        }
        return findAllLectureByMajorType(findOption);
    }

    public void cancelLectureValue(EvaluatePostsToLecture dto) {
        Lecture lecture = lectureRepository.findById(dto.getLectureId());
        lecture.cancelLectureValue(dto);
    }

    public void addLectureValue(EvaluatePostsToLecture dto) {
        Lecture lecture = lectureRepository.findById(dto.getLectureId());
        lecture.addLectureValue(dto);
    }

    public void calcLectureAvg(EvaluatePostsToLecture dto) {
        Lecture lecture = lectureRepository.findById(dto.getLectureId());
        lecture.getLectureAvg();
    }

    public LectureToJsonArray findAllLectureByFindOption(LectureFindOption lectureFindOption) {
        List<LectureResponseDto> dtoList = new ArrayList<>();
        LectureListAndCountDto dto = lectureRepository.findAllLectureByFindOption(lectureFindOption);
        for (Lecture lecture : dto.getLectureList()) {
            dtoList.add(new LectureResponseDto(lecture));
        }

        return new LectureToJsonArray(dtoList, dto.getCount());
    }

    public LectureToJsonArray findAllLectureByMajorType(LectureFindOption lectureFindOption) {
        List<LectureResponseDto> dtoList = new ArrayList<>();
        LectureListAndCountDto dto = lectureRepository.findAllLectureByMajorType(lectureFindOption);
        for (Lecture lecture : dto.getLectureList()) {
            dtoList.add(new LectureResponseDto(lecture));
        }

        return new LectureToJsonArray(dtoList, dto.getCount());
    }

    public LectureToJsonArray findLectureByFindOption(String searchValue, LectureFindOption lectureFindOption) {
        List<LectureResponseDto> dtoList = new ArrayList<>();
        LectureListAndCountDto dto = lectureRepository.findLectureByFindOption(searchValue, lectureFindOption);
        for (Lecture lecture : dto.getLectureList()) {
            dtoList.add(new LectureResponseDto(lecture));
        }

        return new LectureToJsonArray(dtoList, dto.getCount());
    }

    public LectureToJsonArray findLectureByMajorType(String searchValue, LectureFindOption lectureFindOption) {
        List<LectureResponseDto> dtoList = new ArrayList<>();
        LectureListAndCountDto dto = lectureRepository.findLectureByMajorType(searchValue, lectureFindOption);
        for (Lecture lecture : dto.getLectureList()) {
            dtoList.add(new LectureResponseDto(lecture));
        }

        return new LectureToJsonArray(dtoList, dto.getCount());
    }

    public LectureDetailResponseDto findByIdDetail(Long id) {
        Lecture lecture = lectureRepository.findById(id);
        LectureDetailResponseDto dto = new LectureDetailResponseDto(lecture);
        return dto;
    }

    public Lecture findById(Long id) {
        return lectureRepository.findById(id);
    }

    public List<String> findAllMajorType() {
        List<String> resultList = lectureRepository.findAllMajorType();
        return resultList;
    }

    private LectureFindOption createLectureFindOption(Optional<String> option,
        Optional<Integer> page, Optional<String> majorType) {
        return LectureFindOption.builder()
            .orderOption(option)
            .pageNumber(page)
            .majorType(majorType)
            .build();
    }

}
