package api.repository;

import api.domain.SystemItemImport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemItemImportRepository extends CrudRepository<SystemItemImport, Long>, SystemItemImportRepositoryCustom {
}
