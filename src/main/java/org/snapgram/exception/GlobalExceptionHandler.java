package org.snapgram.exception;

import jakarta.validation.ConstraintViolationException;
import org.snapgram.model.response.ErrorResponse;
import org.snapgram.model.response.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
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

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseObject<ErrorResponse> handleValidationException(Exception e, WebRequest request, BindingResult bindingResult) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .path(getPath(request))
                .build();
//        String message = e.getMessage();
        String message = "";

        for (ObjectError error : bindingResult.getAllErrors()) {
            message += error.getDefaultMessage() + ", ";
        }

        if (e instanceof MethodArgumentNotValidException) {
//            int start = message.lastIndexOf("[") + 1;
//            int end = message.lastIndexOf("]") - 1;
//            message = message.substring(start, end);
            errorResponse.setError("Invalid Payload");
            errorResponse.setMessage(message);
        } else if (e instanceof MissingServletRequestParameterException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message);
        } else {
            errorResponse.setError("Invalid Data");
            errorResponse.setMessage(message);
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
        ex.printStackTrace();
        ErrorResponse error = ErrorResponse.builder()
                .error("Internal Server Error")
                .message(ex.getMessage())
                .build();
        return new ResponseObject<>(HttpStatus.UNAUTHORIZED, error);
    }


}
