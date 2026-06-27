package api.service;

import api.domain.SystemItem;
import api.repository.SystemItemAuditRepository;
import api.repository.SystemItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("SystemItemAuditServiceImpl (unit)")
@ExtendWith(MockitoExtension.class)
class SystemItemAuditServiceImplTest {

  private static final ZonedDateTime DATE = ZonedDateTime.parse("2022-02-01T12:00:00Z");

  @Mock
  private SystemItemAuditRepository auditRepository;
  @Mock
  private SystemItemRepository systemItemRepository;
  @InjectMocks
  private SystemItemAuditServiceImpl service;

  private static SystemItem file(String id) {
    return new SystemItem(id, SystemItem.Type.FILE, "/file/" + id, 100L, null, null, DATE);
  }

  @DisplayName("findHistory возвращает null если элемента нет (контроллер отдаст 404)")
  @Test
  void findHistory_returnsNull_whenItemMissing() {
    when(systemItemRepository.findById("X")).thenReturn(Optional.empty());

    var result = service.findHistory("X", null, null);

    assertNull(result);
    verifyNoInteractions(auditRepository);
  }

  @DisplayName("findHistory возвращает ревизии из аудита если элемент существует")
  @Test
  void findHistory_returnsAuditResult_whenItemExists() {
    var item = file("X");
    var revisions = List.of(item);
    when(systemItemRepository.findById("X")).thenReturn(Optional.of(item));
    when(auditRepository.findHistory(item, DATE, null)).thenReturn(revisions);

    var result = service.findHistory("X", DATE, null);

    assertSame(revisions, result);
    verify(auditRepository).findHistory(item, DATE, null);
  }

  @DisplayName("findUpdated делегирует в аудит-репозиторий")
  @Test
  void findUpdated_delegatesToAuditRepository() {
    var updated = List.of(file("X"));
    when(auditRepository.findUpdated(DATE)).thenReturn(updated);

    var result = service.findUpdated(DATE);

    assertSame(updated, result);
    verify(auditRepository).findUpdated(DATE);
  }
}
