package api.dto;

import api.Utils;
import api.domain.SystemItem.Type;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemResponseDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("url")
  private String url;

  @JsonProperty("parentId")
  private String parentId;

  @JsonProperty("size")
  private Long size;

  @JsonProperty("type")
  private Type type;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Utils.DATE_FORMAT)
  @JsonProperty("date")
  private ZonedDateTime date;

  @JsonProperty("children")
  private List<SystemItemResponseDto> children;

}
