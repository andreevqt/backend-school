package api.service;

import api.domain.SystemItem;
import api.repository.SystemItemAuditRepository;
import api.repository.SystemItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class SystemItemAuditServiceImpl implements SystemItemAuditService {

  SystemItemAuditRepository auditRepository;
  SystemItemRepository systemItemRepository;


  @Transactional(readOnly = true)
  @Override
  public List<SystemItem> findHistory(String id, ZonedDateTime dateStart, ZonedDateTime dateEnd) {
    return systemItemRepository.findById(id)
      .map((item) -> auditRepository.findHistory(item, dateStart, dateEnd))
      .orElse(null);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SystemItem> findUpdated(ZonedDateTime to) {
    return auditRepository.findUpdated(to);
  }

}
