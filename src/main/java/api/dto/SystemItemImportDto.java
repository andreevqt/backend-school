package api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemImportDto {

  @Valid
  @NotNull
  @JsonProperty("items")
  private List<SystemItemRequestDto> items;

  @NotNull
  @JsonProperty("updateDate")
  private ZonedDateTime updateDate;

}
