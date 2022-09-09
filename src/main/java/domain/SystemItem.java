package domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
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

  @JoinColumn(name = "parent_id")
  @OneToMany()
  private List<SystemItem> children;

  @Column(name = "date")
  private ZonedDateTime date;

}
