package api.repository;

import api.domain.SystemItem;

import java.time.ZonedDateTime;
import java.util.List;

public interface SystemItemAuditRepository {

  List<SystemItem> findHistory(SystemItem item, ZonedDateTime dateStart, ZonedDateTime dateEnd);

  List<SystemItem> findUpdated(ZonedDateTime to);

}
