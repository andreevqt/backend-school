package api.config;

import api.converters.StringToZonedDateTimeConverter;
import api.serializers.DateDeserializer;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Configuration
public class JacksonConfig {

  private final StringToZonedDateTimeConverter converter;

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> {
      builder.deserializers(new DateDeserializer(ZonedDateTime.class, converter));
    };
  }

}
