package com.twentyfive.apaapilayer.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.common.requests.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderRestoringNotAllowedException.class)
    public ResponseEntity<ApiError> handleRestoringNotAllowedException (OrderRestoringNotAllowedException oex){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, oex.getMessage());
        return  new ResponseEntity<>(apiError,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidItemException.class)
    public ResponseEntity<ApiError> handleIllegalItem (InvalidItemException ex){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "l'item selezionato non esiste! "+ex.getClass().getSimpleName());
        return  new ResponseEntity<>(apiError,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiError> handleIllegalCategory (InvalidCategoryException ex){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "la categoria non esiste! "+ex.getClass().getSimpleName());
        return  new ResponseEntity<>(apiError,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ExistingFieldException.class)
    public ResponseEntity<ApiError> handleExistingField (ExistingFieldException ex){
        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE, "Uno dei campi è unico e non può essere duplicato! "+ex.getClass().getSimpleName());
        return  new ResponseEntity<>(apiError,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElementException (NoSuchElementException ex){
        ApiError apiError = new ApiError(HttpStatus.NO_CONTENT, ex.getMessage()+ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError,HttpStatus.NO_CONTENT);
    }
    @ExceptionHandler(InvalidKeycloakIdRequestException.class)
    public ResponseEntity<ApiError> handleInvalidKeycloakIdRequestException (InvalidKeycloakIdRequestException ex){
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,"Impossibile modificare un customer che non sia quello che ha fatto richiesta! " +ex.getClass().getSimpleName());
        return  new ResponseEntity<>(apiError,HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(InvalidOrderTimeException.class)
    public ResponseEntity<ApiError> handleInvalidOrderTimeException (InvalidOrderTimeException ex) {
        ApiError apiError = new ApiError(HttpStatus.I_AM_A_TEAPOT, "L'orario non è più disponibile! " + ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError, HttpStatus.I_AM_A_TEAPOT);
    }
    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiError> handleInvalidCouponException (InvalidCouponException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Coupon non valido! " + ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(GlobalStatNotFoundException.class)
    public ResponseEntity<ApiError> handleGlobalStatNotFoundException (GlobalStatNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }


    @Data
    @AllArgsConstructor
    public static class ApiError {
        private HttpStatus status;
        private String message;
    }
}
