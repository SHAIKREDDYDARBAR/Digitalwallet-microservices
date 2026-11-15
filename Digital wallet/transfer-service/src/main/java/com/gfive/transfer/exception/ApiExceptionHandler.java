package com.gfive.transfer.exception;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> badRequest(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
}
