package api.mappers;

import api.domain.SystemItem;
import api.dto.SystemItemImportDto;
import api.dto.SystemItemRequestDto;
import api.dto.SystemItemResponseDto;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SystemItemMapperImpl implements SystemItemMapper {

  @Override
  public SystemItem fromDto(SystemItemRequestDto dto, ZonedDateTime date) {
    var size = dto.getSize();
    return new SystemItem(dto.getId(), dto.getType(), dto.getUrl(), size != null ? size : 0, dto.getParentId(), new ArrayList<>(), date);
  }

  @Override
  public List<SystemItem> fromImportDto(SystemItemImportDto importDto) {
    return importDto.getItems().stream()
      .map((item) -> fromDto(item, importDto.getUpdateDate()))
      .collect(Collectors.toList());
  }

  @Override
  public SystemItemResponseDto toDto(SystemItem item) {

    return new SystemItemResponseDto(
      item.getId(), item.getUrl(), item.getParentId(),
      item.getSize(), item.getType(), item.getDate(),
      item.getType() == SystemItem.Type.FOLDER
        ? item.getChildren().stream().map(this::toDto).collect(Collectors.toList())
        : null
    );
  }

}
