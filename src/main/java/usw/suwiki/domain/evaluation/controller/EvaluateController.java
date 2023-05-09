package usw.suwiki.domain.evaluation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usw.suwiki.domain.evaluation.FindByLectureToJson;
import usw.suwiki.domain.evaluation.dto.EvaluatePostsSaveDto;
import usw.suwiki.domain.evaluation.dto.EvaluatePostsUpdateDto;
import usw.suwiki.domain.evaluation.dto.EvaluateResponseByLectureIdDto;
import usw.suwiki.domain.evaluation.dto.EvaluateResponseByUserIdxDto;
import usw.suwiki.domain.evaluation.service.EvaluatePostsService;
import usw.suwiki.global.PageOption;
import usw.suwiki.global.ResponseForm;
import usw.suwiki.global.annotation.ApiLogger;
import usw.suwiki.global.exception.errortype.AccountException;
import usw.suwiki.global.jwt.JwtAgent;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static usw.suwiki.global.exception.ExceptionType.POSTS_WRITE_OVERLAP;
import static usw.suwiki.global.exception.ExceptionType.USER_RESTRICTED;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/evaluate-posts")
public class EvaluateController {

    private final EvaluatePostsService evaluatePostsService;
    private final JwtAgent jwtAgent;

    @ApiLogger(option = "evaluatePosts")
    @GetMapping
    public ResponseEntity<FindByLectureToJson> findByLecture(
            @RequestHeader String Authorization,
            @RequestParam Long lectureId,
            @RequestParam(required = false) Optional<Integer> page
    ) {
        HttpHeaders header = new HttpHeaders();
        jwtAgent.validateJwt(Authorization);
        if (jwtAgent.getUserIsRestricted(Authorization)) {
            throw new AccountException(USER_RESTRICTED);
        }
        List<EvaluateResponseByLectureIdDto> list =
                evaluatePostsService.findEvaluatePostsByLectureId(
                        new PageOption(page),
                        lectureId
                );
        FindByLectureToJson data = new FindByLectureToJson(list);
        if (evaluatePostsService.verifyIsUserWriteEvaluatePost(
                jwtAgent.getId(Authorization), lectureId)) {
            data.setWritten(false);
        }
        return new ResponseEntity<>(data, header, HttpStatus.valueOf(200));
    }

    @ApiLogger(option = "evaluatePosts")
    @PutMapping
    public ResponseEntity<String> updateEvaluatePosts(
            @RequestParam Long evaluateIdx,
            @RequestHeader String Authorization,
            @RequestBody EvaluatePostsUpdateDto dto
    ) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        jwtAgent.validateJwt(Authorization);
        if (jwtAgent.getUserIsRestricted(Authorization)) {
            throw new AccountException(USER_RESTRICTED);
        }
        evaluatePostsService.update(evaluateIdx, dto);
        return new ResponseEntity<>("success", header, HttpStatus.valueOf(200));
    }

    @ApiLogger(option = "evaluatePosts")
    @PostMapping
    public ResponseEntity<String> saveEvaluatePosts(
            @RequestParam Long lectureId,
            @RequestHeader String Authorization,
            @RequestBody EvaluatePostsSaveDto dto
    ) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);

        jwtAgent.validateJwt(Authorization);
        if (jwtAgent.getUserIsRestricted(Authorization)) {
            throw new AccountException(USER_RESTRICTED);
        }
        Long userIdx = jwtAgent.getId(Authorization);
        if (evaluatePostsService.verifyIsUserWriteEvaluatePost(userIdx, lectureId)) {
            evaluatePostsService.save(dto, userIdx, lectureId);
            return new ResponseEntity<>("success", header, HttpStatus.valueOf(200));
        } else {
            throw new AccountException(POSTS_WRITE_OVERLAP);
        }
    }

    @ApiLogger(option = "evaluatePosts")
    @ResponseStatus(OK)
    @GetMapping("/written")
    public ResponseEntity<ResponseForm> findByUser(
            @RequestHeader String Authorization,
            @RequestParam(required = false) Optional<Integer> page
    ) {
        HttpHeaders header = new HttpHeaders();
        jwtAgent.validateJwt(Authorization);
        if (jwtAgent.getUserIsRestricted(Authorization)) {
            throw new AccountException(USER_RESTRICTED);
        }

        List<EvaluateResponseByUserIdxDto> list = evaluatePostsService.findEvaluatePostsByUserId(
                new PageOption(page),
                jwtAgent.getId(Authorization)
        );

        ResponseForm data = new ResponseForm(list);
        return new ResponseEntity<>(data, header, HttpStatus.valueOf(200));
    }

    @ApiLogger(option = "evaluatePosts")
    @ResponseStatus(OK)
    @DeleteMapping
    public String deleteEvaluatePosts(
            @RequestParam Long evaluateIdx,
            @RequestHeader String Authorization
    ) {
        jwtAgent.validateJwt(Authorization);
        if (jwtAgent.getUserIsRestricted(Authorization)) {
            throw new AccountException(USER_RESTRICTED);
        }
        Long userIdx = jwtAgent.getId(Authorization);
        evaluatePostsService.executeDeleteEvaluatePost(evaluateIdx, userIdx);
        return "success";
    }
}