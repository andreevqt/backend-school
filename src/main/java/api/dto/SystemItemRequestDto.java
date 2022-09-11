package api.dto;

import api.domain.SystemItem.Type;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemRequestDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("url")
  private String url;

  @JsonProperty("parentId")
  private String parentId;

  @JsonProperty("size")
  private Long size;

  @JsonProperty("type")
  @Enumerated(EnumType.STRING)
  private Type type;

}
