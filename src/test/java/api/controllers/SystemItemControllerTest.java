package api.controllers;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import api.Main;
import api.domain.SystemItem;
import api.dto.SystemItemImportDto;
import api.dto.SystemItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller для работы с файлами должен:")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
public class SystemItemControllerTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
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
        SystemItem.Type.FILE);

    var body = new SystemItemImportDto(
        List.of(itemDto),
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
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
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

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
  public void imports_shouldReturnbadRequestIfFolderWithSize() throws Exception {

    var itemDto = new SystemItemRequestDto(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        null,
        null,
        128L,
        SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
        List.of(itemDto),
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у папки указан url")
  @Test
  @Transactional
  public void imports_shouldReturnbadRequestIfFolderWithUrl() throws Exception {

    var itemDto = new SystemItemRequestDto(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        "",
        null,
        null,
        SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
        List.of(itemDto),
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 400 если у папки parentId эта же папка")
  @Test
  @Transactional
  public void imports_shouldReturnbadRequestIfParentIdIsTheSameFolder() throws Exception {
    em.persist(new SystemItem("863e1a7a-1304-42ae-943b-179184c077e4",
        SystemItem.Type.FOLDER,
        null,
        0L,
        null,
        null,
        null));

    var itemDto = new SystemItemRequestDto(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        "863e1a7a-1304-42ae-943b-179184c077e4",
        null,
        null,
        SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
        List.of(itemDto),
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @DisplayName("POST imports должно возвращать 200 если parentId существует")
  @Test
  @Transactional
  public void imports_shouldReturnbadRequestIfParentIdExists() throws Exception {
    em.persist(new SystemItem("863e1a7a-1304-42ae-943b-179184c077e3",
        SystemItem.Type.FOLDER,
        null,
        null,
        null,
        null,
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z")));

    var itemDto = new SystemItemRequestDto(
        "863e1a7a-1304-42ae-943b-179184c077e4",
        "863e1a7a-1304-42ae-943b-179184c077e3",
        null,
        null,
        SystemItem.Type.FOLDER);

    var body = new SystemItemImportDto(
        List.of(itemDto),
        ZonedDateTime.parse("2022-02-01T12:00:00.000Z"));

    mockMvc.perform(post("/imports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

}
