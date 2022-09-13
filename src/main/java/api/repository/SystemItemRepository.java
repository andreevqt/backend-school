package api.repository;

import api.domain.SystemItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemItemRepository extends CrudRepository<SystemItem, String> {

  @Query(value = "select coalesce((select sum(size) " +
    "from system_items si " +
    "inner join system_item_imports sii on si.id = sii.child_id " +
    "where sii.parent_id = :id and si.type = 'FILE'), 0)", nativeQuery = true)
  Long calcSize(@Param("id") String id);

  @Query(value = "select si.id, si.parent_id, si.url, si.type, si.date, si.size " +
    "from system_items as si " +
    "inner join (select parent_id, child_id, depth " +
    "from system_item_imports " +
    "where child_id = :id) as sii " +
    "on si.ID = sii.parent_id " +
    "where si.type = 'FOLDER'", nativeQuery = true)
  List<SystemItem> findParentFolders(@Param("id") String id);

  @Query(value = "select si.id, si.parent_id, si.url, si.type, si.date, si.size " +
    "from system_items as si " +
    "inner join (select parent_id, child_id, depth " +
    "from system_item_imports " +
    "where parent_id = :id) as sii " +
    "on si.id = sii.child_id", nativeQuery = true)
  List<SystemItem> findAllDescendants(@Param("id") String id);

  @EntityGraph(attributePaths = "children")
  Optional<SystemItem> findById(String id);

}
