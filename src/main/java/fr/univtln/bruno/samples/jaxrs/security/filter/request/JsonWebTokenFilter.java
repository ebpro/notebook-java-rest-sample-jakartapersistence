package fr.univtln.bruno.samples.jaxrs.security.filter.request;

import fr.univtln.bruno.samples.jaxrs.security.InMemoryLoginModule;
import fr.univtln.bruno.samples.jaxrs.security.MySecurityContext;
import fr.univtln.bruno.samples.jaxrs.security.annotations.JWTAuth;
import io.jsonwebtoken.*;
import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.java.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * This class if a filter for JAX-RS to perform authentication via JWT.
 */
@JWTAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
@Log
public class JsonWebTokenFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    //We inject the data from the acceded resource.
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        //We use reflection on the acceded method to look for security annotations.
        Method method = resourceInfo.getResourceMethod();

        //if its PermitAll access is granted (without specific security context)
        if (method.isAnnotationPresent(PermitAll.class)) return;

        //otherwise, if its DenyAll the access is refused
        if (method.isAnnotationPresent(DenyAll.class)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied to all users").build());
            return;
        }

        //We get the authorization header from the request
        final String authorization = requestContext.getHeaderString(AUTHORIZATION_PROPERTY);

        //We check the credential presence
        if (authorization == null || authorization.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Please provide your credentials").build());
            return;
        }

        //We get the token
        final String compactJwt = authorization.substring(AUTHENTICATION_SCHEME.length()).trim();
        if (!authorization.contains(AUTHENTICATION_SCHEME) || compactJwt.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Please provide correct credentials").build());
            return;
        }

        String username = null;

        //We check the validity of the token
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(InMemoryLoginModule.KEY)
                    .build()
                    .parseSignedClaims(compactJwt);

            username = jws.getPayload().getSubject();


            log.info("USERNAME : " + username);

            //We build a new securitycontext to transmit the security data to JAX-RS
            requestContext.setSecurityContext(MySecurityContext.newInstance(AUTHENTICATION_SCHEME, username));
        } catch (JwtException e) {
            log.info("JWT token error: " + e.getLocalizedMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Wrong JWT token. " + e.getLocalizedMessage()).build());
        }


        //If present, we extract the allowed role's annotation.
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            EnumSet<InMemoryLoginModule.Role> rolesSet =
                    Arrays.stream(rolesAnnotation.value())
                            .map(InMemoryLoginModule.Role::valueOf)
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(InMemoryLoginModule.Role.class)));

            //We check if the role is allowed
            if (!InMemoryLoginModule.isInRoles(rolesSet, username))
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Roles not allowed").build());

        }
    }
}