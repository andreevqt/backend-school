package api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Table(name = "system_items")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemItem {

  public enum Type {
    FILE,
    FOLDER
  }

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  private Type type;

  @Column(name = "url")
  private String url;

  @Column(name = "size")
  private Long size;

  @Column(name = "parent_id")
  private String parentId;

  @JoinColumn(name = "parent_id")
  @OneToMany(fetch = FetchType.LAZY)
  private List<SystemItem> children;

  @Column(name = "date")
  private ZonedDateTime date;

}
