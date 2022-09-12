package api.controller;


import api.Utils;
import api.dto.SystemItemImportDto;
import api.exceptions.ResourceNotFoundException;
import api.mappers.SystemItemMapper;
import api.service.SystemItemAuditService;
import api.service.SystemItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Validated
@AllArgsConstructor
@RestController
public class SystemItemController {

  private final SystemItemService systemItemService;
  private final SystemItemAuditService systemItemAuditService;
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

  @GetMapping("/updates")
  public ResponseEntity<?> updates(@RequestParam(value = "date") ZonedDateTime date) {
    return ResponseEntity.ok(Map.of(
      "items", mapper.toHistoryDtos(systemItemAuditService.findUpdated(date)))
    );
  }

  @GetMapping("/node/{id}/history")
  public ResponseEntity<?> history(@PathVariable("id") @Pattern(regexp = Utils.UUID_PATTERN) String id,
                                   @RequestParam(value = "dateStart", required = false) ZonedDateTime dateStart,
                                   @RequestParam(value = "dateEnd", required = false) ZonedDateTime dateEnd) {
    return Optional.ofNullable(systemItemAuditService.findHistory(id, dateStart, dateEnd))
      .map((result) -> ResponseEntity.ok(Map.of("items", mapper.toHistoryDtos(result))))
      .orElseThrow(ResourceNotFoundException::new);
  }

}
