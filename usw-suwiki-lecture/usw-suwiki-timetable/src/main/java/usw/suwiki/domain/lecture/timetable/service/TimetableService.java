package usw.suwiki.domain.lecture.timetable.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.ExceptionCode;
import usw.suwiki.core.exception.TimetableException;
import usw.suwiki.domain.lecture.timetable.Timetable;
import usw.suwiki.domain.lecture.timetable.TimetableCell;
import usw.suwiki.domain.lecture.timetable.TimetableRepository;
import usw.suwiki.domain.lecture.timetable.dto.TimetableRequest;
import usw.suwiki.domain.lecture.timetable.dto.TimetableResponse;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableService {
  private final TimetableRepository timetableRepository;

  @Transactional(readOnly = true)
  public List<TimetableResponse.Simple> getMyAllTimetables(Long userId) {
    return timetableRepository.findAllByUserId(userId).stream()
      .map(TimetableMapper::toSimple)
      .toList();
  }

  @Transactional(readOnly = true)
  public TimetableResponse.Detail loadTimetable(Long timetableId) {
    Timetable timetable = loadById(timetableId);
    return TimetableMapper.toDetail(timetable);
  }

  public void create(Long userId, TimetableRequest.Description request) {
    Timetable timetable = new Timetable(userId, request.getName(), request.getYear(), request.getSemester());
    timetableRepository.save(timetable);
  }

  public void update(Long userId, Long timetableId, TimetableRequest.Description request) {
    Timetable timetable = loadById(timetableId);
    timetable.validateAuthor(userId);
    timetable.update(request.getName(), request.getYear(), request.getSemester());
  }

  @Async
  public void bulkInsert(Long userId, List<TimetableRequest.Bulk> requests) {
    var timetables = requests.stream()
      .map(request -> TimetableMapper.toTimetable(userId, request))
      .toList();

    timetableRepository.saveAll(timetables);
  }

  public void delete(Long userId, Long timetableId) {
    Timetable timetable = loadById(timetableId);
    timetable.validateAuthor(userId);
    timetableRepository.delete(timetable);
  }

  public void addCell(Long userId, Long timetableId, TimetableRequest.Cell request) {
    Timetable timetable = loadById(timetableId);
    timetable.validateAuthor(userId);

    TimetableCell cell = TimetableMapper.toCell(request);
    timetable.addCell(cell);
  }

  public void updateCell(Long userId, Long timetableId, int cellIdx, TimetableRequest.UpdateCell request) {
    Timetable timetable = loadById(timetableId);
    timetable.validateAuthor(userId);

    TimetableCell cell = TimetableMapper.toCell(request);
    timetable.updateCell(cellIdx, cell);
  }

  public void deleteCell(Long userId, Long timetableId, int cellIdx) {
    Timetable timetable = loadById(timetableId);
    timetable.validateAuthor(userId);
    timetable.removeCell(cellIdx);
  }

  private Timetable loadById(Long timetableId) {
    return timetableRepository.findById(timetableId)
      .orElseThrow(() -> new TimetableException(ExceptionCode.TIMETABLE_NOT_FOUND));
  }
}
