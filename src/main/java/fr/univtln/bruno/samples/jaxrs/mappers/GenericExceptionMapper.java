package fr.univtln.bruno.samples.jaxrs.mappers;

import fr.univtln.bruno.samples.jaxrs.exceptions.BusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;

/**
 * The type Generic exception mapper automatically produces a HTTP Response
 *  if a Exception is thrown with a default status (500).
 */
@SuppressWarnings("unused")
@Provider
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    public Response toResponse(Exception exception) {
        log.info("--->"+exception.toString());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new BusinessException(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
