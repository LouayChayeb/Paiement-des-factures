package tn.pi.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tn.pi.dtos.ErrorResponseDTO;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    @ExceptionHandler(ThisAccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> HandleCustomerNotFoundException(ThisAccountNotFoundException ex, WebRequest request) {
        ErrorResponseDTO errorResponsedto = new ErrorResponseDTO(request.getDescription(false  ), HttpStatus.NOT_FOUND, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponsedto, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccountAllReadyException.class)
    public ResponseEntity<ErrorResponseDTO> HandleCustomerAllReadyException(AccountAllReadyException ex, WebRequest request) {
        ErrorResponseDTO errorResponsedto = new ErrorResponseDTO(request.getDescription(false  ), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponsedto, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccountWithPhoneNumberExists.class)

    public ResponseEntity<ErrorResponseDTO> HandlePhoneAlreadyExistsException(AccountWithPhoneNumberExists ex, WebRequest request) {
        ErrorResponseDTO errorResponseDto = new ErrorResponseDTO(request.getDescription(false  ), HttpStatus.BAD_REQUEST, ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }
}
