package usw.suwiki.api.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import usw.suwiki.common.pagination.PageOption;
import usw.suwiki.common.response.ResponseForm;
import usw.suwiki.domain.notice.dto.NoticeResponse;
import usw.suwiki.domain.notice.service.NoticeService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.statistics.log.MonitorTarget.NOTICE;

@RestController
@RequestMapping(value = "/notices")
@RequiredArgsConstructor
public class NoticeControllerV2 {
  private final NoticeService noticeService;

  @Statistics(NOTICE)
  @GetMapping("/v2")
  @ResponseStatus(OK)
  public ResponseForm getAllNoticesV2(@RequestParam(required = false) Optional<Integer> page) {
    var response = noticeService.getAllNotices(PageOption.offset(page));
    return new ResponseForm(response);
  }

  @Statistics(NOTICE)
  @GetMapping("/v2/{noticeId}")
  @ResponseStatus(OK)
  public ResponseForm getNoticeV2(@PathVariable Long noticeId) {
    NoticeResponse.Detail response = noticeService.getNotice(noticeId);
    return new ResponseForm(response);
  }
}
