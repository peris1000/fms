package com.zimono.trg.a.config;

import com.zimono.trg.a.config.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesNotFound() {
        Response r = handler.toResponse(new NotFoundException("Item not found"));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), r.getStatus());
        var body = (GlobalExceptionHandler.ErrorResponse) r.getEntity();
        assertNotNull(body.getTimestamp());
        assertEquals("Item not found", body.getMessage());
        assertNull(body.getDetails());
    }

    @Test
    void handlesIllegalArgument_asConflict() {
        Response r = handler.toResponse(new IllegalArgumentException("Duplicate"));
        assertEquals(Response.Status.CONFLICT.getStatusCode(), r.getStatus());
        var body = (GlobalExceptionHandler.ErrorResponse) r.getEntity();
        assertEquals("Duplicate", body.getMessage());
    }

    @Test
    void handlesWebApplicationException_passesStatus() {
        WebApplicationException ex = new WebApplicationException("Bad req", Response.Status.BAD_REQUEST);
        Response r = handler.toResponse(ex);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
        var body = (GlobalExceptionHandler.ErrorResponse) r.getEntity();
        assertEquals("Bad req", body.getMessage());
    }

    @Test
    void handlesConstraintViolation_asBadRequest() {
        Response r = handler.toResponse(new ConstraintViolationException(null));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
        var body = (GlobalExceptionHandler.ErrorResponse) r.getEntity();
        assertEquals("Validation failed", body.getMessage());
    }

    @Test
    void handlesGenericException_asInternalServerError() {
        Response r = handler.toResponse(new Exception("Something went wrong"));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), r.getStatus());
        var body = (GlobalExceptionHandler.ErrorResponse) r.getEntity();
        assertEquals("Something went wrong", body.getMessage());
        assertEquals(Exception.class.getName(), body.getDetails());
        assertNotNull(body.getTimestamp());
    }
}
