package org.snapgram.exception;

import jakarta.validation.ConstraintViolationException;
import org.snapgram.model.response.ErrorResponse;
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

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request, BindingResult bindingResult) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
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
        } else if (e instanceof ConstraintViolationException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message.substring(message.indexOf(" ") + 1));
        } else {
            errorResponse.setError("Invalid Data");
            errorResponse.setMessage(message);
        }

        return errorResponse;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Data")
                .path(getPath(request))
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAccessDenied(WebRequest request) {
        return ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Access Denied")
                .path(getPath(request))
                .message("You are not authorized to access this resource")
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        ex.printStackTrace();
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .build();
    }


}
