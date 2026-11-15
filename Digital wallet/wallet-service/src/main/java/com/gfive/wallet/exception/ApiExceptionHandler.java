package com.gfive.wallet.exception;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> badRequest(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> conflict(Exception e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));}
}
