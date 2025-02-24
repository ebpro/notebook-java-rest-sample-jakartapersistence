package fr.univtln.bruno.samples.jaxrs.mappers;

import fr.univtln.bruno.samples.jaxrs.exceptions.BusinessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;

/**
 * The type Business exception mapper automatically produces a HTTP Response
 * if a BusinessException is thrown.
 */
@SuppressWarnings("unused")
@Provider
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {
    public Response toResponse(BusinessException ex) {
        return Response.status(ex.getStatus())
                .entity(ex)
                .build();
    }
}
