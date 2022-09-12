package api.dto;

import api.Utils;
import api.domain.SystemItem.Type;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class SystemItemHistoryDto {

  private String id;

  private String url;

  private String parentId;

  private Type type;

  private Long size;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Utils.DATE_FORMAT)
  private ZonedDateTime date;

}
