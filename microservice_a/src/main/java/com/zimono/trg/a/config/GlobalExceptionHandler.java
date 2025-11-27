package com.zimono.trg.a.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


@Provider
@Singleton
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        if (LOG.isDebugEnabled()) {
            LOG.error("Unhandled exception", exception);
        }
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.timestamp = Instant.now();
        errorResponse.message = "An unexpected error occurred";

        if (exception instanceof NotFoundException) {
            errorResponse.message = exception.getMessage();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }

        if (exception instanceof IllegalArgumentException) {
            errorResponse.message = exception.getMessage();
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        }

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            errorResponse.message = exception.getMessage();
            return Response.status(webEx.getResponse().getStatus())
                    .entity(errorResponse)
                    .build();
        }

        if (exception instanceof ConstraintViolationException) {
            errorResponse.message = "Validation failed";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        if (exception.getMessage() != null) {
            errorResponse.message = exception.getMessage();
            errorResponse.details = exception.getClass().getName();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }

    @RegisterForReflection
    public static class ErrorResponse {
        public Instant timestamp;
        public String message;
        public String details;

        // Getters and setters for serialization
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}