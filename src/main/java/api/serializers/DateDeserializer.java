package api.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.ZonedDateTime;

public class DateDeserializer extends StdDeserializer<ZonedDateTime> {

  private final Converter<String, ZonedDateTime> converter;

  public DateDeserializer(Class<?> vc, Converter<String, ZonedDateTime> converter) {
    super(vc);
    this.converter = converter;
  }

  @Override
  public ZonedDateTime deserialize(JsonParser parser, DeserializationContext ctx)
      throws IOException, JacksonException {
    return converter.convert(parser.getText());
  }

}
