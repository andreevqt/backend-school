package api.dto;

import api.Utils;
import api.domain.SystemItem.Type;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemRequestDto {

  @NotBlank
  @Pattern(regexp = Utils.UUID_PATTERN)
  @JsonProperty("id")
  private String id;

  @Size(max = 255)
  @JsonProperty("url")
  private String url;

  @Pattern(regexp = Utils.UUID_PATTERN)
  @JsonProperty("parentId")
  private String parentId;

  @Min(1)
  @JsonProperty("size")
  private Long size;

  @JsonProperty("type")
  @Enumerated(EnumType.STRING)
  private Type type;

  @AssertTrue
  public boolean isValid() {
    if (type == Type.FOLDER && url != null) {
      return false;
    }

    if (type == Type.FOLDER && size != null) {
      return false;
    }

    if (type == Type.FILE && size == null) {
      return false;
    }

    return !(type == Type.FILE && url == null);
  }

}
