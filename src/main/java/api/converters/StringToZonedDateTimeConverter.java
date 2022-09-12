package api.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Component
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

  @Override
  public ZonedDateTime convert(String source) {
    try {
      return ZonedDateTime.parse(source);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(source).atStartOfDay(ZoneOffset.of("Z"));
    }
  }

}
