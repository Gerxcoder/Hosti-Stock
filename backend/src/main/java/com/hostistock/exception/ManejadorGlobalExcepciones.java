package com.hostistock.exception;

import com.hostistock.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Convierte excepciones en respuestas JSON uniformes. Sin esto Spring devuelve HTML.
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensajes = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

        return construirRespuesta(HttpStatus.BAD_REQUEST, "Error de validación", mensajes, request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> manejarStockInsuficiente(
            StockInsuficienteException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.CONFLICT, "Stock insuficiente", ex.getMessage(), request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoIlegal(
            IllegalArgumentException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Petición inválida", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarExcepcionGeneral(
            Exception ex, HttpServletRequest request) {
        return construirRespuesta(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor",
            ex.getMessage(),
            request
        );
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(
            HttpStatus status, String error, String mensaje, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            error,
            mensaje,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}