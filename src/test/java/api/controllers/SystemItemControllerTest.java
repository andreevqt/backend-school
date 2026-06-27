package api.controllers;

import api.Main;
import api.domain.SystemItem;
import api.dto.SystemItemImportDto;
import api.dto.SystemItemRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Controller для работы с файлами")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
public class SystemItemControllerTest {

  private static final String DATE = "2022-02-01T12:00:00Z";

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

  @DisplayName("POST imports должно возвращать 200 если передан корректный файл")
  @Test
  @Transactional
  public void imports_shouldReturnOkIfValidFile() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "/file/url1",
      null,
      128L,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string("OK"));
  }

  @DisplayName("POST imports должно возвращать 400 если переданна некорректная дата")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfDateNotCorrect() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "/file/url1",
      null,
      128L,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      null
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у файла не указан size")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFileHasNoSize() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "/file/url1",
      null,
      null,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у файла не указан url")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFileHasNoUrl() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      null,
      128L,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если url слишком длинный")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFilesUrlTooLong() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "QymT9YKaZqI9VnPiRrOQ7XA9QfdrBvzQfBwpSs55qIzFtx32GOmShnqAJLbfllnT4779vmory8xu9vQ3K1fjdDxvaJMdpTbj5WUktR8d8aKYv12OeGNJM9i5FrUN5YGl9PhqBLDJFzSQwtx1mFU6CWZRSkYGQcPaRGD7igYJzwObpnR1iK9pXxX3CDdIhXkW1DAilWOnNMGMQtm4IER91XHPihCJgsMYK5M9FldYxmmGR9W3H9tH1xn3sxjN8eIx",
      null,
      128L,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 200 если у файла url пустой")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFileHasEmptyUrl() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "",
      null,
      128L,
      SystemItem.Type.FILE
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());
  }

  @DisplayName("POST imports должно возвращать 200 если передана корректная папка")
  @Test
  @Transactional
  public void imports_shouldReturnOkIfValidFolder() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      null,
      null,
      SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string("OK"));
  }

  @DisplayName("POST imports должно возвращать 400 если у папки указан размер")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFolderWithSize() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      null,
      128L,
      SystemItem.Type.FOLDER
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у папки указан url")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfFolderWithUrl() throws Exception {
    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      "",
      null,
      null,
      SystemItem.Type.FOLDER
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у папки parentId эта же папка")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfParentIdIsTheSameFolder() throws Exception {
    em.persist(
      new SystemItem(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        SystemItem.Type.FOLDER,
        null,
        0L,
        null,
        null,
        null
      )
    );

    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      SystemItem.Type.FOLDER
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 200 если parentId существует")
  @Test
  @Transactional
  public void imports_shouldReturnOktIfParentIdExists() throws Exception {
    em.persist(new SystemItem(
      "863e1a7a-1304-42ae-943b-179184c077e3",
      SystemItem.Type.FOLDER,
      null,
      null,
      null,
      null,
      ZonedDateTime.parse(DATE))
    );

    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      "863e1a7a-1304-42ae-943b-179184c077e3",
      null,
      SystemItem.Type.FOLDER
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());
  }

  @DisplayName("POST imports должно возвращать 400 если parent не папка")
  @Test
  @Transactional
  public void imports_shouldReturnBadRequestIfParentIsDirectory() throws Exception {
    em.persist(new SystemItem(
        "863e1a7a-1304-42ae-943b-179184c077e3",
        SystemItem.Type.FILE,
        "",
        120L,
        null,
        null,
        ZonedDateTime.parse(DATE)
      )
    );

    var itemDto = new SystemItemRequestDto(
      "863e1a7a-1304-42ae-943b-179184c077e4",
      null,
      "863e1a7a-1304-42ae-943b-179184c077e3",
      null,
      SystemItem.Type.FOLDER
    );

    var body = new SystemItemImportDto(
      List.of(itemDto),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно импортировать вложенную цепочку при обратном порядке в одном запросе")
  @Test
  @Transactional
  public void imports_shouldImportReverseOrderedChainInSingleRequest() throws Exception {
    var rootId = "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1";
    var childId = "069cb8d7-bbdd-47d3-ad8f-82ef4c269df2";
    var fileId = "863e1a7a-1304-42ae-943b-179184c077e4";

    var file = new SystemItemRequestDto(fileId, "/file/url1", childId, 128L, SystemItem.Type.FILE);
    var child = new SystemItemRequestDto(childId, null, rootId, null, SystemItem.Type.FOLDER);
    var root = new SystemItemRequestDto(rootId, null, null, null, SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
      List.of(file, child, root),
      ZonedDateTime.parse(DATE)
    );

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string("OK"));

    em.flush();
    em.clear();

    mockMvc.perform(get("/nodes/" + rootId)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(rootId))
      .andExpect(jsonPath("$.size").value(128))
      .andExpect(jsonPath("$.children[0].id").value(childId))
      .andExpect(jsonPath("$.children[0].size").value(128))
      .andExpect(jsonPath("$.children[0].children[0].id").value(fileId))
      .andExpect(jsonPath("$.children[0].children[0].size").value(128));
  }

  @DisplayName("GET /nodes/{id} должно возвращать корректный ответ")
  @Transactional
  @Test
  public void imports_shouldReturnCorrectStructure() throws Exception {
    persistTree();

    mockMvc.perform(get("/nodes/" + TREE.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(TREE.getId()))
      .andExpect(jsonPath("$.size").value(TREE.getSize()))
      .andExpect(jsonPath("$.date").value(DATE))
      .andExpect(jsonPath("$.children[0].id").value(TREE.getChildren().get(0).getId()))
      .andExpect(jsonPath("$.children[0].size").value(TREE.getChildren().get(0).getSize()))
      .andExpect(jsonPath("$.children[0].children[0].id").value(TREE.getChildren().get(0)
        .getChildren().get(0).getId()));
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
}
