package api.repository;

import api.domain.SystemItemImport;

public interface SystemItemImportRepositoryCustom {

  void moveTree(String id, String to);

  void insertNode(String parentId, String childId);

  void delete(String id);

  SystemItemImport save(SystemItemImport node);

}
