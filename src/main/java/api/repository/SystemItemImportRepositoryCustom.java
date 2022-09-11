package api.repository;

import api.domain.SystemItemImport;

import java.util.Optional;

public interface SystemItemImportRepositoryCustom {

  Optional<SystemItemImport> findRoot(String id);

  void moveTree(String id, String to);

  void insertNode(String parentId, String childId);

  void delete(String id);

  SystemItemImport save(SystemItemImport node);

}
