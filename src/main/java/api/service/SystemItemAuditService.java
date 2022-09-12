package api.service;

import api.domain.SystemItem;

import java.time.ZonedDateTime;
import java.util.List;

public interface SystemItemAuditService {

  List<SystemItem> findHistory(String id, ZonedDateTime dateStart, ZonedDateTime dateEnd);

  List<SystemItem> findUpdated(ZonedDateTime to);

}
