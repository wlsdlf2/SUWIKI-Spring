package usw.suwiki.api.notice;

import io.github.hejow.restdocs.document.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import usw.suwiki.common.pagination.PageOption;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.ResponseValidator;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.domain.notice.NoticeRepository;
import usw.suwiki.domain.notice.dto.NoticeRequest;
import usw.suwiki.domain.user.User;
import usw.suwiki.test.fixture.Fixtures;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.common.test.Tag.NOTICE;
import static usw.suwiki.common.test.extension.AssertExtension.expectExceptionJsonPath;
import static usw.suwiki.common.test.support.Pair.parameter;
import static usw.suwiki.core.exception.ExceptionType.NOTICE_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.USER_RESTRICTED;

@AcceptanceTest
public class NoticeAcceptanceTest extends AcceptanceTestSupport {
  @Autowired
  private NoticeRepository noticeRepository;

  @Autowired
  private Fixtures fixtures;

  private User admin;
  private String accessToken;

  @BeforeEach
  public void setup() {
    admin = fixtures.관리자_생성();
    accessToken = fixtures.토큰_생성(admin);
  }

  @Nested
  class 공지_생성_테스트 {
    private final String endpoint = "/notice/";

    @Test
    void 공지_생성_성공() throws Exception {
      // expected
      var identifier = "create-notice";
      var summary = "[Admin 토큰 필요] 공지 생성 API";
      var description = "공지를 생성하는 API 입니다."; // todo
      var tag = NOTICE;
      var expectedResults = "success";

      // given
      var request = new NoticeRequest.Create("공지사항 제목", "공지사항 내용");

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      var notices = noticeRepository.findByNoticeList(new PageOption(Optional.of(1)));
      assertAll(
        () -> assertThat(notices.get(0)).isNotNull(),
        () -> assertThat(notices.get(0).getTitle()).isEqualTo(request.getTitle()),
        () -> assertThat(notices.get(0).getContent()).isEqualTo(request.getContent())
      );

      // result validation
      ResponseValidator.validateHtml(result, status().isOk(), expectedResults);

      // Non DOCS
    }

    @Test
    void 공지_생성_실패_권한_없음() throws Exception {
      // given
      var userToken = fixtures.토큰_생성();
      var request = new NoticeRequest.Create("공지사항 제목", "공지사항 내용");

      // when
      var result = post(Uri.of(endpoint), userToken, request);

      // then
      result.andExpectAll(
        status().isForbidden(),
        expectExceptionJsonPath(result, USER_RESTRICTED)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-notice-fail-forbidden-user")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 공지_수정_테스트 {
    private final String endpoint = "/notice/";
    private final String paramKey = "noticeId";

    @Test
    void 공지_수정_성공() throws Exception {
      // expected
      var identifier = "update-notice";
      var summary = "[Admin 토큰 필요] 공지 수정 API";
      var description = "공지를 생성하는 API 입니다."; // todo
      var expectedResults = "success";

      // given
      var notice = fixtures.공지사항_생성();
      var request = new NoticeRequest.Update("수정된 공지사항 제목", "수정된 공지사항 내용");

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, notice.getId()));

      // then
      var evaluatePosts = noticeRepository.findByNoticeList(new PageOption(Optional.of(1)));
      assertAll(
        () -> assertThat(evaluatePosts.get(0)).isNotNull(),
        () -> assertThat(evaluatePosts.get(0).getTitle()).isEqualTo(request.getTitle()),
        () -> assertThat(evaluatePosts.get(0).getContent()).isEqualTo(request.getContent())
      );

      // result validation
      ResponseValidator.validateHtml(result, status().isOk(), expectedResults);

      // Non DOCS
    }

    @Test
    void 공지_수정_실패_권한_없음() throws Exception {
      // given
      var notice = fixtures.공지사항_생성();
      var userAccessToken = fixtures.토큰_생성();

      var request = new NoticeRequest.Update("수정된 공지사항 제목", "수정된 공지사항 내용");

      // when
      var result = put(Uri.of(endpoint), userAccessToken, request, parameter(paramKey, notice.getId()));

      // then
      result.andExpectAll(
        status().isForbidden(),
        expectExceptionJsonPath(result, USER_RESTRICTED)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-notice-fail-forbidden-user")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 공지_수정_실패_잘못된_파라미터() throws Exception {
      // given
      var request = new NoticeRequest.Update("수정된 공지사항 제목", "수정된 공지사항 내용");

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, NOTICE_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-notice-fail-not-exist-notice")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 공지_삭제_테스트 {
    private final String endpoint = "/notice/";
    private final String paramKey = "noticeId";

    @Test
    void 공자_삭제_성공() throws Exception {
      // given
      var notice = fixtures.공지사항_생성();

      // when´
      var result = delete(Uri.of(endpoint), accessToken, null, parameter(paramKey, notice.getId()));

      // then
      result.andExpect(status().isOk());

      var notices = noticeRepository.findByNoticeList(new PageOption(Optional.of(1)));
      assertThat(notices).isEmpty();

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-notice")
          .summary("[Admin 토큰 필요] 공지 삭제 API")
          .description("공지를 삭제하는 API 입니다.")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 공지_삭제_실패_잘못된_파라미터() throws Exception {
      // given

      // when
      var result = delete(Uri.of(endpoint), accessToken, null, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, NOTICE_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-notice-fail-not-exist-notice")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 공지_삭제_실패_권한_없음() throws Exception {
      // given
      var notice = fixtures.공지사항_생성();
      var userAccessToken = fixtures.토큰_생성();

      // when´
      var result = delete(Uri.of(endpoint), userAccessToken, null, parameter(paramKey, notice.getId()));

      // then
      result.andExpectAll(
        status().isForbidden(),
        expectExceptionJsonPath(result, USER_RESTRICTED)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-notice-fail-forbidden-user")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 공지_단일_조회_테스트 {
    private final String endpoint = "/notice/";
    private final String paramKey = "noticeId";

    @Test
    void 공지_조회_성공() throws Exception {
      // given
      var notice = fixtures.공지사항_생성();

      // when
      var result = get(Uri.of(endpoint), accessToken, parameter(paramKey, notice.getId()));

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data.id").value(notice.getId()),
        jsonPath("$.data.title").value(notice.getTitle()),
        jsonPath("$.data.content").value(notice.getContent()),
        jsonPath("$.data.modifiedDate").value(notice.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("get-notice")
          .summary("[토큰 필요] 공지 조회 API")
          .description("공지를 조회하는 API 입니다.")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 공지_조회_실패_잘못된_파라미터() throws Exception {
      // given

      // when
      var result = get(Uri.of(endpoint), accessToken, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, NOTICE_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("get-notice-fail-not-exist-notice")
          .tag(NOTICE)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Test
  void 공지_리스트_조회_성공() throws Exception {
    // given
    final int DEFAULT_SIZE = 10;

    var totalPage = 3;
    var requestPage = 1;
    var size = DEFAULT_SIZE * totalPage;

    var notices = fixtures.공지사항_여러개_생성(size);

    // when
    var result = get(Uri.of("/notice/all"), accessToken, parameter("page", requestPage));

    // then
    result.andExpectAll(
      status().isOk(),
      jsonPath("$.data.length()").value(DEFAULT_SIZE),
      jsonPath("$.data.[0].id").value(notices.get(notices.size() - 1).getId()),
      jsonPath("$.data.[0].title").value(notices.get(notices.size() - 1).getTitle()),
      jsonPath("$.data.[0].modifiedDate").value(notices.get(notices.size() - 1).getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
    );

    // docs
    result.andDo(
      RestDocument.builder()
        .identifier("get-notice-list-success")
        .summary("공지 리스트 조회 API")
        .description("공지 리스트 조회 API 입니다.")
        .tag(NOTICE)
        .result(result)
        .generateDocs()
    );
  }
}
