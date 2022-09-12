package api.controller;


import api.dto.SystemItemImportDto;
import api.exceptions.ResourceNotFoundException;
import api.mappers.SystemItemMapper;
import api.service.SystemItemService;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import api.Utils;

@Validated
@AllArgsConstructor
@RestController
public class SystemItemController {

  private final SystemItemService systemItemService;
  private final SystemItemMapper mapper;

  @PostMapping("/imports")
  public ResponseEntity<?> doImport(@Valid @RequestBody SystemItemImportDto importDto) {
    var items = mapper.fromImportDto(importDto);
    systemItemService.doImport(items, importDto.getUpdateDate());
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/nodes/{id}")
  public ResponseEntity<?> get(@PathVariable("id") @Pattern(regexp = Utils.UUID_PATTERN) String id) {
    return systemItemService.get(id)
      .map((item) -> ResponseEntity.ok(mapper.toDto(item)))
      .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> delete(@PathVariable("id") @Pattern(regexp = Utils.UUID_PATTERN) String id) {
    return systemItemService.get(id).map((item) -> {
      systemItemService.delete(id);
      return ResponseEntity.ok("OK");
    }).orElseThrow(ResourceNotFoundException::new);
  }

}
