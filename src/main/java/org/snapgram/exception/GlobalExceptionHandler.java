package org.snapgram.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.ErrorResponse;
import org.snapgram.dto.response.ResponseObject;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler(KeyGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseObject<ErrorResponse> handleKeyPairGenerationException(KeyGenerationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Key Generation Error")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseObject<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("File Size Limit Exceeded")
                .path(getPath(request))
                .message("The uploaded file size exceeds the maximum limit = " + ex.getMaxUploadSize() + " bytes")
                .build();
        return new ResponseObject<>(HttpStatus.PAYLOAD_TOO_LARGE, error);
    }

    @ExceptionHandler(UploadFileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseObject<ErrorResponse> handleUploadFileException(UploadFileException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Upload File Error")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleOAuth2AuthenticationException(OAuth2AuthenticationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Token")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("JWT Token Expired")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseObject<ErrorResponse> handleRedisConnectionFailureException(RedisConnectionFailureException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Redis Connection Failure.")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseObject<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Unauthorized")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.UNAUTHORIZED, error);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseObject<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Unauthorized")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.UNAUTHORIZED, error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseObject<ErrorResponse> handleUsernameNotFoundException(UserNotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("User Not Found")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.NOT_FOUND, error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseObject<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Resource Not Found")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.NOT_FOUND, error);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleValidationException(Exception e, WebRequest request, BindingResult bindingResult) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .path(getPath(request))
                .build();
        StringBuilder message = new StringBuilder();

        for (ObjectError error : bindingResult.getAllErrors()) {
            message.append(error.getDefaultMessage()).append(", ");
        }

        if (e instanceof MethodArgumentNotValidException) {
            errorResponse.setError("Invalid Payload");
            errorResponse.setMessage(message.toString());
        } else if (e instanceof MissingServletRequestParameterException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message.toString());
        }
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        log.error("Invalid Data", ex);
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    // validate in @RequestParam
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleMethodValidationException(HandlerMethodValidationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseObject<ErrorResponse> handleAccessDenied(WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .error("Access Denied")
                .path(getPath(request))
                .message("You are not authorized to access this resource")
                .build();
        return new ResponseObject<>(HttpStatus.UNAUTHORIZED, error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseObject<ErrorResponse> handleException(Exception ex) {
        log.error("Internal Server Error", ex);
        ErrorResponse error = ErrorResponse.builder()
                .error("Internal Server Error")
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, error);
    }


}
