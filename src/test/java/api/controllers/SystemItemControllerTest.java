package api.controllers;

import api.Main;
import api.domain.SystemItem;
import api.dto.SystemItemImportDto;
import api.dto.SystemItemRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;


@DisplayName("Controller для работы с файлами")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
public class SystemItemControllerTest {

  private static final String IMPORTS = "/imports";
  private static final String NODES = "/nodes/";
  private static final String DELETE_PATH = "/delete/";
  private static final String UPDATES = "/updates";

  private static String historyPath(String id) {
    return "/node/" + id + "/history";
  }

  private static final String DATE = "2022-02-01T12:00:00Z";
  private static final String LATER_DATE = "2022-02-02T12:00:00Z";

  private static final String ROOT = "11111111-1111-4111-8111-111111111111";
  private static final String CHILD = "22222222-2222-4222-9222-222222222222";
  private static final String FILE_A = "33333333-3333-4333-a333-333333333333";
  private static final String FILE_B = "44444444-4444-4444-b444-444444444444";
  private static final String FOLDER_B = "55555555-5555-4555-8555-555555555555";
  private static final String MISSING = "66666666-6666-4666-9666-666666666666";
  private static final String NOT_A_UUID = "not-a-uuid";

  private static final SystemItem TREE = new SystemItem(
    "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
    SystemItem.Type.FOLDER,
    null,
    256L,
    null,
    new ArrayList<>(List.of(new SystemItem(
      "069cb8d7-bbdd-47d3-ad8f-82ef4c269df2",
      SystemItem.Type.FOLDER,
      null,
      128L,
      "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
      new ArrayList<>(List.of(new SystemItem(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        SystemItem.Type.FILE,
        "/file/url1",
        128L,
        "069cb8d7-bbdd-47d3-ad8f-82ef4c269df2",
        null,
        ZonedDateTime.parse(DATE)
      ))),
      ZonedDateTime.parse(DATE)
    ), new SystemItem(
      "863e1a7a-1304-42ae-943b-179184c077e3",
      SystemItem.Type.FILE,
      "/file/url2",
      128L,
      "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
      null,
      ZonedDateTime.parse(DATE)
    ))),
    ZonedDateTime.parse(DATE)
  );

  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private EntityManager em;
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private MockMvc mockMvc;
  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private JdbcTemplate jdbc;

  @AfterEach
  void cleanup() {
    jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
    for (var table : List.of("system_item_system_item_aud", "system_items_aud", "revinfo",
      "system_items", "system_item_imports")) {
      jdbc.update("delete from " + table);
    }
    jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
  }

  private static SystemItemRequestDto file(String id, String parentId, Long size) {
    return new SystemItemRequestDto(id, "/file/" + id, parentId, size, SystemItem.Type.FILE);
  }

  private static SystemItemRequestDto folder(String id, String parentId) {
    return new SystemItemRequestDto(id, null, parentId, null, SystemItem.Type.FOLDER);
  }

  private ResultActions postImport(SystemItemImportDto body) throws Exception {
    return mockMvc.perform(post(IMPORTS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(body))
      .accept(MediaType.APPLICATION_JSON));
  }

  private ResultActions performImport(String date, SystemItemRequestDto... items) throws Exception {
    return postImport(new SystemItemImportDto(List.of(items), ZonedDateTime.parse(date)));
  }

  private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request.accept(MediaType.APPLICATION_JSON));
  }

  private void detach() {
    em.flush();
    em.clear();
  }

  private void persistTree() {
    persistItem(TREE);
  }

  private void persistItem(SystemItem item) {
    em.persist(item);
    var children = item.getChildren();
    if (children != null) {
      children.forEach(this::persistItem);
    }
  }

  @Nested
  @DisplayName("POST /imports")
  class Imports {

    @DisplayName("возвращает 200 если передан корректный файл")
    @Test
    @Transactional
    public void shouldReturnOkIfValidFile() throws Exception {
      performImport(DATE, file(FILE_A, null, 128L))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
    }

    @DisplayName("возвращает 400 если передана некорректная дата")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfDateNotCorrect() throws Exception {
      postImport(new SystemItemImportDto(List.of(file(FILE_A, null, 128L)), null))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у файла не указан size")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFileHasNoSize() throws Exception {
      performImport(DATE, file(FILE_A, null, null))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у файла не указан url")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFileHasNoUrl() throws Exception {
      performImport(DATE, new SystemItemRequestDto(FILE_A, null, null, 128L, SystemItem.Type.FILE))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если url слишком длинный")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFilesUrlTooLong() throws Exception {
      var tooLongUrl =
        "QymT9YKaZqI9VnPiRrOQ7XA9QfdrBvzQfBwpSs55qIzFtx32GOmShnqAJLbfllnT4779vmory8xu9vQ3K1fjdDxvaJMdpTbj5WUktR8d8aKYv12OeGNJM9i5FrUN5YGl9PhqBLDJFzSQwtx1mFU6CWZRSkYGQcPaRGD7igYJzwObpnR1iK9pXxX3CDdIhXkW1DAilWOnNMGMQtm4IER91XHPihCJgsMYK5M9FldYxmmGR9W3H9tH1xn3sxjN8eIx";
      performImport(DATE, new SystemItemRequestDto(FILE_A, tooLongUrl, null, 128L, SystemItem.Type.FILE))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 200 если у файла url пустой")
    @Test
    @Transactional
    public void shouldReturnOkIfFileHasEmptyUrl() throws Exception {
      performImport(DATE, new SystemItemRequestDto(FILE_A, "", null, 128L, SystemItem.Type.FILE))
        .andExpect(status().isOk());
    }

    @DisplayName("возвращает 200 если передана корректная папка")
    @Test
    @Transactional
    public void shouldReturnOkIfValidFolder() throws Exception {
      performImport(DATE, folder(FILE_A, null))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
    }

    @DisplayName("возвращает 400 если у папки указан размер")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFolderWithSize() throws Exception {
      performImport(DATE, new SystemItemRequestDto(FILE_A, null, null, 128L, SystemItem.Type.FOLDER))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у папки указан url")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFolderWithUrl() throws Exception {
      performImport(DATE, new SystemItemRequestDto(FILE_A, "", null, null, SystemItem.Type.FOLDER))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у папки parentId эта же папка")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfParentIdIsTheSameFolder() throws Exception {
      em.persist(new SystemItem(FILE_A, SystemItem.Type.FOLDER, null, 0L, null, null, null));

      performImport(DATE, folder(FILE_A, FILE_A))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 200 если parentId существует")
    @Test
    @Transactional
    public void shouldReturnOkIfParentIdExists() throws Exception {
      em.persist(new SystemItem(FILE_B, SystemItem.Type.FOLDER, null, null, null, null,
        ZonedDateTime.parse(DATE)));

      performImport(DATE, folder(FILE_A, FILE_B))
        .andExpect(status().isOk());
    }

    @DisplayName("возвращает 400 если parent не папка")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfParentIsNotFolder() throws Exception {
      em.persist(new SystemItem(FILE_B, SystemItem.Type.FILE, "", 120L, null, null,
        ZonedDateTime.parse(DATE)));

      performImport(DATE, folder(FILE_A, FILE_B))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если в запросе два элемента с одинаковым id")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfDuplicateIdInRequest() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L), file(FILE_A, null, 200L))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у родителя нет такого id")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfParentMissing() throws Exception {
      performImport(DATE, file(FILE_A, MISSING, 100L))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если у файла size равен 0")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfFileSizeZero() throws Exception {
      performImport(DATE, file(FILE_A, null, 0L))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если id имеет неверный формат")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfIdInvalidFormat() throws Exception {
      performImport(DATE, file(NOT_A_UUID, null, 100L))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если parentId имеет неверный формат")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfParentIdInvalidFormat() throws Exception {
      performImport(DATE, new SystemItemRequestDto(FILE_A, "/file/a", NOT_A_UUID, 100L, SystemItem.Type.FILE))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если items отсутствует")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfItemsMissing() throws Exception {
      mockMvc.perform(post(IMPORTS)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"updateDate\":\"" + DATE + "\"}")
          .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 при смене типа существующего элемента")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfTypeChanged() throws Exception {
      performImport(DATE, folder(FILE_A, null)).andExpect(status().isOk());
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isBadRequest());
    }

    @DisplayName("импортирует вложенную цепочку при обратном порядке в одном запросе")
    @Test
    @Transactional
    public void shouldImportReverseOrderedChainInSingleRequest() throws Exception {
      var file = file(FILE_A, CHILD, 128L);
      var child = folder(CHILD, ROOT);
      var root = folder(ROOT, null);

      performImport(DATE, file, child, root)
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ROOT))
        .andExpect(jsonPath("$.size").value(128))
        .andExpect(jsonPath("$.children[0].id").value(CHILD))
        .andExpect(jsonPath("$.children[0].size").value(128))
        .andExpect(jsonPath("$.children[0].children[0].id").value(FILE_A))
        .andExpect(jsonPath("$.children[0].children[0].size").value(128));
    }

    @DisplayName("обновляет существующий элемент (upsert)")
    @Test
    @Transactional
    public void shouldUpdateExistingItem() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE,
        new SystemItemRequestDto(FILE_A, "/file/updated", null, 200L, SystemItem.Type.FILE))
        .andExpect(status().isOk());

      detach();

      perform(get(NODES + FILE_A))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(200))
        .andExpect(jsonPath("$.url").value("/file/updated"))
        .andExpect(jsonPath("$.date").value(LATER_DATE));
    }

    @DisplayName("пересчитывает размер родителя при обновлении файла")
    @Test
    @Transactional
    public void shouldRecalculateParentSizeOnReimport() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, ROOT, 300L)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(300));
    }

    @DisplayName("переносит элемент и пересчитывает размеры обеих папок")
    @Test
    @Transactional
    public void shouldMoveItemAndRecalculateBothFolders() throws Exception {
      performImport(DATE, folder(ROOT, null), folder(FOLDER_B, null), file(FILE_A, ROOT, 100L))
        .andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, FOLDER_B, 150L)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(0));

      perform(get(NODES + FOLDER_B))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(150));
    }

    @DisplayName("переносит элемент в корень (parentId=null)")
    @Test
    @Transactional
    public void shouldReparentToNull() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(0));

      perform(get(NODES + FILE_A))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.parentId").value(nullValue()));
    }

    @DisplayName("считает размер вложенного дерева при прямом порядке")
    @Test
    @Transactional
    public void shouldComputeSizeForNestedTreeInOrder() throws Exception {
      performImport(DATE, folder(ROOT, null), folder(CHILD, ROOT), file(FILE_A, CHILD, 50L))
        .andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.children[0].id").value(CHILD))
        .andExpect(jsonPath("$.children[0].size").value(50))
        .andExpect(jsonPath("$.children[0].children[0].id").value(FILE_A))
        .andExpect(jsonPath("$.children[0].children[0].size").value(50));
    }

    @DisplayName("проставляет дату импорта всем предкам")
    @Test
    @Transactional
    public void shouldPropagateImportDateToAllAncestors() throws Exception {
      performImport(DATE, folder(ROOT, null), folder(CHILD, ROOT), file(FILE_A, CHILD, 100L))
        .andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, CHILD, 200L)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(LATER_DATE))
        .andExpect(jsonPath("$.children[0].id").value(CHILD))
        .andExpect(jsonPath("$.children[0].date").value(LATER_DATE));
    }
  }

  @Nested
  @DisplayName("GET /nodes/{id}")
  class Nodes {

    @DisplayName("возвращает корректную структуру дерева")
    @Test
    @Transactional
    public void shouldReturnFullTreeStructure() throws Exception {
      persistTree();

      perform(get(NODES + TREE.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TREE.getId()))
        .andExpect(jsonPath("$.size").value(TREE.getSize()))
        .andExpect(jsonPath("$.date").value(DATE))
        .andExpect(jsonPath("$.children[0].id").value(TREE.getChildren().get(0).getId()))
        .andExpect(jsonPath("$.children[0].size").value(TREE.getChildren().get(0).getSize()))
        .andExpect(jsonPath("$.children[0].children[0].id").value(TREE.getChildren().get(0)
          .getChildren().get(0).getId()));
    }

    @DisplayName("возвращает 404 если элемент не найден")
    @Test
    @Transactional
    public void shouldReturnNotFoundIfItemDoesNotExist() throws Exception {
      perform(get(NODES + MISSING))
        .andExpect(status().isNotFound());
    }

    @DisplayName("возвращает 400 если id имеет неверный формат")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfIdInvalid() throws Exception {
      perform(get(NODES + NOT_A_UUID))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает children=null и url для файла")
    @Test
    @Transactional
    public void shouldReturnNullChildrenForFile() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      detach();

      perform(get(NODES + FILE_A))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("FILE"))
        .andExpect(jsonPath("$.size").value(100))
        .andExpect(jsonPath("$.url").value("/file/" + FILE_A))
        .andExpect(jsonPath("$.children").value(nullValue()));
    }

    @DisplayName("возвращает пустой массив children и size=0 для пустой папки")
    @Test
    @Transactional
    public void shouldReturnEmptyChildrenForEmptyFolder() throws Exception {
      performImport(DATE, folder(ROOT, null)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("FOLDER"))
        .andExpect(jsonPath("$.size").value(0))
        .andExpect(jsonPath("$.children").isArray())
        .andExpect(jsonPath("$.children.length()").value(0));
    }

    @DisplayName("ответ с ошибкой содержит поля code и message")
    @Test
    @Transactional
    public void errorResponseShouldExposeCodeAndMessage() throws Exception {
      perform(get(NODES + MISSING))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("Item not found"));

      perform(get(NODES + NOT_A_UUID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value("Validation Failed"));
    }
  }

  @Nested
  @DisplayName("DELETE /delete/{id}")
  class Delete {

    @DisplayName("возвращает 404 если элемент не найден")
    @Test
    @Transactional
    public void shouldReturnNotFoundIfItemDoesNotExist() throws Exception {
      perform(delete(DELETE_PATH + MISSING))
        .andExpect(status().isNotFound());
    }

    @DisplayName("возвращает 400 если id имеет неверный формат")
    @Test
    @Transactional
    public void shouldReturnBadRequestIfIdInvalid() throws Exception {
      perform(delete(DELETE_PATH + NOT_A_UUID))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("удаляет файл")
    @Test
    @Transactional
    public void shouldRemoveFile() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      detach();

      perform(delete(DELETE_PATH + FILE_A)).andExpect(status().isOk());

      detach();

      perform(get(NODES + FILE_A)).andExpect(status().isNotFound());
    }

    @DisplayName("удаляет папку вместе с дочерними элементами")
    @Test
    @Transactional
    public void shouldRemoveFolderWithDescendants() throws Exception {
      performImport(DATE, folder(ROOT, null), folder(CHILD, ROOT), file(FILE_A, CHILD, 50L),
        folder(FOLDER_B, null)).andExpect(status().isOk());

      detach();

      perform(delete(DELETE_PATH + ROOT)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT)).andExpect(status().isNotFound());
      perform(get(NODES + CHILD)).andExpect(status().isNotFound());
      perform(get(NODES + FILE_A)).andExpect(status().isNotFound());
      perform(get(NODES + FOLDER_B)).andExpect(status().isOk());
    }

    @DisplayName("пересчитывает размер родителя после удаления файла")
    @Test
    @Transactional
    public void shouldRecalculateParentSizeAfterChildDeleted() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L), file(FILE_B, ROOT, 50L))
        .andExpect(status().isOk());

      detach();

      perform(delete(DELETE_PATH + FILE_A)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(50));
    }

    @DisplayName("пересчитывает размер предка при удалении вложенной папки")
    @Test
    @Transactional
    public void shouldUpdateAncestorSizeAfterSubfolderDeleted() throws Exception {
      performImport(DATE, folder(ROOT, null), folder(CHILD, ROOT), file(FILE_A, CHILD, 50L))
        .andExpect(status().isOk());

      detach();

      perform(delete(DELETE_PATH + CHILD)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(0));
    }

    @DisplayName("не меняет дату родителя, но пересчитывает его размер")
    @Test
    @Transactional
    public void shouldNotUpdateAncestorDate() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L)).andExpect(status().isOk());

      detach();

      perform(delete(DELETE_PATH + FILE_A)).andExpect(status().isOk());

      detach();

      perform(get(NODES + ROOT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(0))
        .andExpect(jsonPath("$.date").value(DATE));
    }
  }

  @Nested
  @DisplayName("GET /updates")
  class Updates {

    @DisplayName("возвращает 400 если не передан date")
    @Test
    public void shouldReturnBadRequestIfDateMissing() throws Exception {
      perform(get(UPDATES))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 400 если date невалидна")
    @Test
    public void shouldReturnBadRequestIfDateInvalid() throws Exception {
      perform(get(UPDATES).param("date", "not-a-date"))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает файлы, обновлённые за последние 24 часа")
    @Test
    public void shouldReturnFilesUpdatedInLast24Hours() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      perform(get(UPDATES).param("date", DATE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(FILE_A))
        .andExpect(jsonPath("$.items[0].size").value(100));
    }

    @DisplayName("не включает папки")
    @Test
    public void shouldExcludeFolders() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L)).andExpect(status().isOk());

      perform(get(UPDATES).param("date", DATE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(FILE_A))
        .andExpect(jsonPath("$.items[0].type").value("FILE"));
    }

    @DisplayName("возвращает пустой список если в окне нет файлов")
    @Test
    public void shouldReturnEmptyListWhenNoFilesInWindow() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      perform(get(UPDATES).param("date", "2022-02-03T12:00:00Z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(0));
    }

    @DisplayName("включает нижнюю границу окна и исключает всё за пределами 24 часов")
    @Test
    public void shouldIncludeLowerBoundaryAndExcludeBeyondWindow() throws Exception {
      var to = "2022-02-02T00:00:00Z";
      var lowerBoundary = "2022-02-01T00:00:00Z"; // ровно to - 24h, должен попасть
      var beyond = "2022-01-31T23:00:00Z";        // за пределами окна, не должен попасть

      performImport(lowerBoundary, file(FILE_A, null, 100L)).andExpect(status().isOk());
      performImport(beyond, file(FILE_B, null, 100L)).andExpect(status().isOk());

      perform(get(UPDATES).param("date", to))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(FILE_A));
    }
  }

  @Nested
  @DisplayName("GET /node/{id}/history")
  class History {

    @DisplayName("возвращает 400 если id имеет неверный формат")
    @Test
    public void shouldReturnBadRequestIfIdInvalid() throws Exception {
      perform(get(historyPath(NOT_A_UUID)))
        .andExpect(status().isBadRequest());
    }

    @DisplayName("возвращает 404 если элемент не найден")
    @Test
    public void shouldReturnNotFoundIfItemDoesNotExist() throws Exception {
      perform(get(historyPath(MISSING)))
        .andExpect(status().isNotFound());
    }

    @DisplayName("возвращает 404 для удалённого элемента")
    @Test
    public void shouldReturnNotFoundForDeletedItem() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());
      perform(delete(DELETE_PATH + FILE_A)).andExpect(status().isOk());

      perform(get(historyPath(FILE_A)))
        .andExpect(status().isNotFound());
    }

    @DisplayName("возвращает одну ревизию для только что созданного элемента")
    @Test
    public void shouldReturnSingleRevisionForNewItem() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());

      perform(get(historyPath(FILE_A)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].size").value(100));
    }

    @DisplayName("возвращает все ревизии элемента")
    @Test
    public void shouldReturnRevisions() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, null, 200L)).andExpect(status().isOk());

      perform(get(historyPath(FILE_A)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].id").value(FILE_A))
        // history order is unspecified — assert both revisions are present by size
        .andExpect(jsonPath("$.items[*].size", containsInAnyOrder(100, 200)));
    }

    @DisplayName("фильтрует ревизии по dateStart")
    @Test
    public void shouldFilterByDateStart() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, null, 200L)).andExpect(status().isOk());

      perform(get(historyPath(FILE_A)).param("dateStart", LATER_DATE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        // only the LATER_DATE revision (size 200) must survive the dateStart filter
        .andExpect(jsonPath("$.items[0].size").value(200));
    }

    @DisplayName("фильтрует ревизии по dateEnd (полуинтервал)")
    @Test
    public void shouldFilterByDateEnd() throws Exception {
      performImport(DATE, file(FILE_A, null, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, null, 200L)).andExpect(status().isOk());

      perform(get(historyPath(FILE_A)).param("dateEnd", LATER_DATE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].size").value(100));
    }

    @DisplayName("история папки отражает изменение её размера при обновлении вложенного файла")
    @Test
    public void shouldRecordFolderSizeChangeWhenDescendantFileUpdated() throws Exception {
      performImport(DATE, folder(ROOT, null), file(FILE_A, ROOT, 100L)).andExpect(status().isOk());
      performImport(LATER_DATE, file(FILE_A, ROOT, 200L)).andExpect(status().isOk());

      perform(get(historyPath(ROOT)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[*].size", containsInAnyOrder(100, 200)));
    }
  }
}
