package api.controller;


import api.dto.SystemItemImportDto;
import api.exceptions.ResourceNotFoundException;
import api.mappers.SystemItemMapper;
import api.service.SystemItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class SystemItemController {

  private final SystemItemService systemItemService;
  private final SystemItemMapper mapper;

  @PostMapping("/imports")
  public ResponseEntity<?> doImport(@RequestBody SystemItemImportDto importDto) {
    var items = mapper.fromImportDto(importDto);
    systemItemService.doImport(items, importDto.getUpdateDate());
    return ResponseEntity.ok("ok");
  }

  @GetMapping("/node/{id}")
  public ResponseEntity<?> get(@PathVariable String id) {
    return systemItemService.get(id)
      .map((item) -> ResponseEntity.ok(mapper.toDto(item)))
      .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> delete(@PathVariable String id) {
    return systemItemService.get(id).map((item) -> {
      systemItemService.delete(id);
      return ResponseEntity.ok("deleted");
    }).orElseThrow(ResourceNotFoundException::new);
  }

}
