package api.service;

import api.domain.SystemItem;
import api.exceptions.SystemItemDuplicateException;
import api.exceptions.SystemItemParentNotFoundException;
import api.exceptions.SystemItemWrongTypeException;
import api.repository.SystemItemImportRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("SystemItemServiceImpl (unit)")
@ExtendWith(MockitoExtension.class)
class SystemItemServiceImplTest {

  private static final ZonedDateTime DATE = ZonedDateTime.parse("2022-02-01T12:00:00Z");

  @Mock
  private SystemItemRepository itemRepository;
  @Mock
  private SystemItemImportRepository importsRepository;
  @InjectMocks
  private SystemItemServiceImpl service;

  private static SystemItem file(String id, String parentId, Long size) {
    return new SystemItem(id, SystemItem.Type.FILE, "/file/" + id, size, parentId, null, DATE);
  }

  private static SystemItem folder(String id, String parentId) {
    return new SystemItem(id, SystemItem.Type.FOLDER, null, null, parentId, null, DATE);
  }

  @DisplayName("get делегирует в findWithChildrenById")
  @Test
  void get_delegatesToFindWithChildrenById() {
    var item = folder("R", null);
    when(itemRepository.findWithChildrenById("R")).thenReturn(Optional.of(item));

    var result = service.get("R");

    assertSame(item, result.orElseThrow());
    verify(itemRepository).findWithChildrenById("R");
  }

  @DisplayName("doImport бросает Duplicate если в запросе два одинаковых id и ничего не пишет")
  @Test
  void doImport_throwsOnDuplicateIdsInRequest() {
    var a = file("F", null, 100L);
    var b = file("F", null, 200L);

    assertThrows(SystemItemDuplicateException.class,
      () -> service.doImport(List.of(a, b), DATE));

    verify(itemRepository, never()).save(any());
    verifyNoInteractions(importsRepository);
  }

  @DisplayName("doImport бросает ParentNotFound если родителя нет нигде")
  @Test
  void doImport_throwsParentNotFound_whenParentMissing() {
    when(itemRepository.findById("P")).thenReturn(Optional.empty());

    assertThrows(SystemItemParentNotFoundException.class,
      () -> service.doImport(List.of(file("F", "P", 100L)), DATE));

    verify(itemRepository, never()).save(any());
  }

  @DisplayName("doImport создаёт новый корневой элемент и заводит узел в closure")
  @Test
  void doImport_createsNewRootItem() {
    var root = folder("R", null);
    when(itemRepository.findById("R")).thenReturn(Optional.empty());
    when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(itemRepository.findParentFolders("R")).thenReturn(List.of());

    service.doImport(List.of(root), DATE);

    verify(itemRepository).save(root);
    verify(importsRepository).insertNode(null, "R");
  }

  @DisplayName("doImport обновляет существующий элемент (upsert размера)")
  @Test
  void doImport_updatesExistingItem() {
    var existing = file("F", null, 100L);
    when(itemRepository.findById("F")).thenReturn(Optional.of(existing));
    when(itemRepository.findParentFolders("F")).thenReturn(List.of());

    service.doImport(List.of(file("F", null, 200L)), DATE);

    assertEquals(200L, existing.getSize());
    verify(itemRepository).save(existing);
    verifyNoInteractions(importsRepository);
  }

  @DisplayName("doImport бросает WrongType при смене типа существующего элемента")
  @Test
  void doImport_throwsWrongType_whenTypeChanges() {
    when(itemRepository.findById("X")).thenReturn(Optional.of(folder("X", null)));

    assertThrows(SystemItemWrongTypeException.class,
      () -> service.doImport(List.of(file("X", null, 100L)), DATE));

    verify(itemRepository, never()).save(any());
    verifyNoInteractions(importsRepository);
  }

  @DisplayName("delete удаляет поддерево, чистит closure и пересчитывает размер предка (без смены даты)")
  @Test
  void delete_removesSubtreeAndRecalculatesAncestor() {
    var parent = folder("R", null);
    var descendants = List.<SystemItem>of();
    when(itemRepository.findParentFolders("C")).thenReturn(List.of(parent));
    when(itemRepository.findAllDescendants("C")).thenReturn(descendants);
    when(itemRepository.findById("R")).thenReturn(Optional.of(parent));
    when(itemRepository.calcSize("R")).thenReturn(0L);

    service.delete("C");

    verify(itemRepository).deleteById("C");
    verify(itemRepository).deleteAll(descendants);
    verify(importsRepository).delete("C");
    verify(itemRepository).save(parent);
    assertEquals(0L, parent.getSize());
    assertEquals(DATE, parent.getDate()); // delete passes null date -> ancestor date unchanged
  }
}
