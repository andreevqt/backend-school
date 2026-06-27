package api.repository;

import api.domain.SystemItemImport;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class SystemItemImportRepositoryCustomImpl implements SystemItemImportRepositoryCustom {

  @PersistenceContext
  EntityManager em;

  @Override
  public void moveTree(String id, String to) {
    em.createNativeQuery("delete " +
        "from system_item_imports " +
        "where child_id in (select a.child_id from (select child_id from system_item_imports where parent_id = :id) as a) " +
        "and parent_id in (select a.parent_id from (select parent_id from system_item_imports where child_id = :id and parent_id != child_id) as a)")
      .setParameter("id", id)
      .executeUpdate();

    // move to the new location
    em.createNativeQuery("insert into system_item_imports (parent_id, child_id, depth) " +
        "select a.parent_id, b.child_id, a.depth + b.depth + 1 " +
        "from system_item_imports a " +
        "cross join system_item_imports b " +
        "where a.child_id = :to and b.parent_id = :id ")
      .setParameter("id", id)
      .setParameter("to", to)
      .executeUpdate();
  }

  @Override
  public void insertNode(String parentId, String childId) {
    // create import
    save(new SystemItemImport(null, childId, childId, 0L));
    em.createNativeQuery("insert into system_item_imports (parent_id, child_id, depth) " +
        "select parent_id, :childId, depth + 1 " +
        "from system_item_imports " +
        "where child_id = :parentId")
      .setParameter("childId", childId)
      .setParameter("parentId", parentId)
      .executeUpdate();
  }

  @Override
  public void delete(String id) {
    em.createNativeQuery("delete " +
        "from system_item_imports " +
        "where child_id = :id")
      .setParameter("id", id)
      .executeUpdate();

    em.createNativeQuery("delete " +
        "from system_item_imports " +
        "where child_id in (select a.child_id from (" +
        "select child_id " +
        "from system_item_imports " +
        "where parent_id = :id) a)")
      .setParameter("id", id)
      .executeUpdate();
  }

  @Override
  public SystemItemImport save(SystemItemImport node) {
    if (node.getId() == null) {
      em.persist(node);
      return node;
    }

    return em.merge(node);
  }

}
