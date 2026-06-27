package api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

import java.time.format.DateTimeParseException;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

  @ExceptionHandler({
      SystemItemException.class,
      ConstraintViolationException.class,
      MethodArgumentNotValidException.class,
      ServletRequestBindingException.class,
      TypeMismatchException.class,
      MissingServletRequestParameterException.class,
      DateTimeParseException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<?> handleBadRequest() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("code", HttpStatus.BAD_REQUEST.value(), "message", "Validation Failed"));
  }

  @ExceptionHandler(value = { ResourceNotFoundException.class })
  public ResponseEntity<?> handleNotFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("code", HttpStatus.NOT_FOUND.value(), "message", "Item not found"));
  }

  @ExceptionHandler(value = { Exception.class })
  public ResponseEntity<?> handleServerError(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("code", HttpStatus.INTERNAL_SERVER_ERROR.value(), "message", "Internal Server Error"));
  }

}
