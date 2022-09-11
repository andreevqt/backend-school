package api.service;

import api.domain.SystemItem;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface SystemItemService {

  void doImport(List<SystemItem> items, ZonedDateTime date);

  Optional<SystemItem> get(String id);

  void delete(String id);

}
