package api.repository;

import api.domain.SystemItem;
import lombok.AllArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
@AllArgsConstructor
public class SystemItemAuditRepositoryImpl implements SystemItemAuditRepository {

  private final AuditReader reader;

  @PersistenceContext
  private final EntityManager em;

  @Override
  public List<SystemItem> findHistory(SystemItem item, ZonedDateTime dateStart, ZonedDateTime dateEnd) {
    var query = reader.createQuery()
      .forRevisionsOfEntity(SystemItem.class, true, false)
      .add(AuditEntity.id().eq(item.getId()));

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
    return em.createNativeQuery("select sii1.id, sii1.parent_id, sii1.size, sii1.type, sii1.url, sii1.date " +
          "from SYSTEM_ITEMS_AUD sii1 " +
          "inner join (" +
          " select id, max(date) as date " +
          " from SYSTEM_ITEMS_AUD " +
          " where TYPE = 'FILE' and date_mod = true and date >= :from and date <= :to group by id" +
          ") sii2 " +
          "on sii1.ID = sii2.id and sii1.date = sii2.date",
        SystemItem.class)
      .setParameter("from", from.toString())
      .setParameter("to", to.toString())
      .getResultList();
  }
}
