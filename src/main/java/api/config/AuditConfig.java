package api.config;

import lombok.AllArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;


@Configuration
@AllArgsConstructor
public class AuditConfig {

  private final EntityManagerFactory entityManagerFactory;

  @Bean
  AuditReader auditReader() {
    return AuditReaderFactory.get(entityManagerFactory.createEntityManager());
  }

}
