package api.mappers;

import api.domain.SystemItem;
import api.dto.SystemItemRequestDto;
import api.dto.SystemItemImportDto;
import api.dto.SystemItemResponseDto;

import java.time.ZonedDateTime;
import java.util.List;

public interface SystemItemMapper {

  SystemItem fromDto(SystemItemRequestDto dto, ZonedDateTime date);

  List<SystemItem> fromImportDto(SystemItemImportDto importDto);

  SystemItemResponseDto toDto(SystemItem item);

}
