package usw.suwiki.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

  // user
  INVALID_EMAIL_FORMAT("USER01", "올바른 이메일 형식이 아닙니다.", BAD_REQUEST),
  USER_NOT_FOUND("USER02", "사용자가 존재하지 않습니다.", NOT_FOUND),
  LOGIN_FAIL("USER03", "아이디 혹은 비밀번호를 확인해주세요.", BAD_REQUEST),
  PASSWORD_ERROR("USER04", "비밀번호를 확인해주세요.", BAD_REQUEST),
  OUT_OF_POINT("USER05", "포인트가 부족합니다.", BAD_REQUEST),
  LOGIN_REQUIRED("USER06", "로그인이 필요합니다.", FORBIDDEN),
  USER_RESTRICTED("USER07", "접근 권한이 없는 사용자 입니다. 관리자에게 문의하세요.", FORBIDDEN),
  BLACKLIST("USER08", "블랙리스트 대상입니다. 이용할 수 없습니다.", FORBIDDEN),
  DUPLICATED_ID_OR_EMAIL("USER09", "아이디 혹은 이메일이 중복됩니다.", BAD_REQUEST),
  SAME_PASSWORD_WITH_OLD("USER10", "이전 비밀번호와 동일하게 변경할 수 없습니다.", BAD_REQUEST),
  NOT_AN_AUTHOR("USER11", "해당 데이터의 수정 및 삭제는 작성자의 권한입니다.", FORBIDDEN), // todo: 수정 가능한지 확인하기

  // exam post
  EXAM_POST_NOT_FOUND("EXAM_POST01", "해당 시험정보를 찾을 수 없습니다.", NOT_FOUND),
  ALREADY_PURCHASED("EXAM_POST02", "이미 구매한 시험정보 입니다.", BAD_REQUEST),
  ALREADY_WROTE_EXAM_POST("EXAM_POST03", "이미 작성한 강의 평가입니다.", BAD_REQUEST),

  // evaluate post
  EVALUATE_POST_NOT_FOUND("EVALUATE_POST01", "해당 강의평가를 찾을 수 없습니다.", NOT_FOUND),

  // auth
  EXPIRED_TOKEN("AUTH01", "토큰이 만료되었습니다 다시 로그인 해주세요", UNAUTHORIZED), // 401..?
  INVALID_TOKEN("AUTH02", "토큰이 유효하지 않습니다.", BAD_REQUEST),
  EMAIL_NOT_AUTHED("AUTH03", "이메일 인증을 받지 않은 사용자 입니다.", UNAUTHORIZED),

  // lecture
  LECTURE_NOT_FOUND("LECTURE01", "해당 강의에 대한 정보를 찾을 수 없습니다.", NOT_FOUND),

  // notice
  NOTICE_NOT_FOUND("NOTICE01", "해당 공지사항을 찾을 수 없습니다.", NOT_FOUND),

  // report
  REPORTED_POST_NOT_FOUND("REPORT01", "해당 신고된 게시글을 찾을 수 없습니다.", NOT_FOUND),

  // major
  MAJOR_NOT_FOUND("MAJOR01", "해당 즐겨찾기 된 과목을 찾을 수 없습니다.", NOT_FOUND),
  ALREADY_FAVORITE_MAJOR("MAJOR02", "이미 즐겨찾기 된 과목입니다.", BAD_REQUEST), // BAD..?

  // timetable
  TIMETABLE_NOT_FOUND("TIMETABLE01", "존재하지 않는 시간표입니다.", NOT_FOUND),
  INVALID_TIMETABLE_SEMESTER("TIMETABLE02", "유효하지 않은 학기명입니다.", BAD_REQUEST),
  INVALID_TIMETABLE_CELL_DAY("TIMETABLE04", "유효하지 않은 요일입니다.", BAD_REQUEST),
  INVALID_TIMETABLE_CELL_COLOR("TIMETABLE03", "유효하지 않은 셀 색상입니다.", BAD_REQUEST),
  INVALID_TIMETABLE_CELL_SCHEDULE("TIMETABLE05", "유효하지 않은 셀 스케줄입니다.", BAD_REQUEST),
  OVERLAPPED_TIMETABLE_CELL_SCHEDULE("TIMETABLE06", "중복되는 시간표 셀입니다.", CONFLICT),

  // application
  SERVER_ERROR("SERVER01", "서버 오류 입니다. 관리자에게 문의해주세요", INTERNAL_SERVER_ERROR),
  SEND_MAIL_FAILED("SERVER02", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR),
  BAD_MAIL_REQUEST("SERVER03", "잘못된 메일 발송 요청입니다.", INTERNAL_SERVER_ERROR),
  AUTHORIZATION_NOT_PROCESSED("SERVER04", "인증이 요청하지 않은 API에서 잘못된 파싱을 시도하고 있습니다.", INTERNAL_SERVER_ERROR),
  EXTERNAL_API_FAILED("SERVER05", "외부 API 요청에 실패했습니다.", INTERNAL_SERVER_ERROR),

  COMMON_CLIENT_ERROR("CLIENT01", "기타 클라이언트 에러입니다.", BAD_REQUEST), // ..?
  INVALID_CLIENT_OS("CLIENT02", "유효하지 않은 클라이언트 OS 입니다.", BAD_REQUEST),
  PARAMETER_VALIDATION_FAIL("CLIENT03", "파라미터가 올바르지 않습니다.", BAD_REQUEST),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;

  public int getStatus() {
    return status.value();
  }
}
