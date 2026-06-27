package api.repository;

import api.domain.SystemItem;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class SystemItemAuditRepositoryImpl implements SystemItemAuditRepository {

  private final AuditReader reader;

  @PersistenceContext
  private EntityManager em;

  public SystemItemAuditRepositoryImpl(AuditReader reader) {
    this.reader = reader;
  }

  @Override
  public List<SystemItem> findHistory(SystemItem item, ZonedDateTime dateStart, ZonedDateTime dateEnd) {
    var query = reader.createQuery()
      .forRevisionsOfEntity(SystemItem.class, true, false)
      .add(AuditEntity.id().eq(item.getId()))
      .add(AuditEntity.property("date").hasChanged());

    if (dateStart != null) {
      query.add(AuditEntity.property("date").ge(dateStart));
    }

    if (dateEnd != null) {
      query.add(AuditEntity.property("date").lt(dateEnd));
    }

    return query.getResultList();
  }

  @Override
  public List<SystemItem> findUpdated(ZonedDateTime to) {
    var from = to.minusHours(24);
    return em.createNativeQuery(
        "select distinct si.id, si.parent_id, si.size, si.type, si.url, si.date " +
          "from system_items si " +
          "inner join (" +
          " select id " +
          " from system_items_aud " +
          " where TYPE = 'FILE' and date_mod = true and date >= :from and date <= :to " +
          ") sia " +
          "on si.id = sia.id ",
        SystemItem.class)
      .setParameter("from", from.toString())
      .setParameter("to", to.toString())
      .getResultList();
  }
}
