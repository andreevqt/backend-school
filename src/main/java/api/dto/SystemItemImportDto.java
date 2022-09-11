package api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemImportDto {

  @JsonProperty("items")
  private List<SystemItemRequestDto> items;

  @JsonProperty("updateDate")
  private ZonedDateTime updateDate;

}
