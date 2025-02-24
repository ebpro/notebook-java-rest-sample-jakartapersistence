package fr.univtln.bruno.samples.jaxrs.resources;

import fr.univtln.bruno.samples.jaxrs.security.InMemoryLoginModule;
import fr.univtln.bruno.samples.jaxrs.security.User;
import fr.univtln.bruno.samples.jaxrs.security.annotations.BasicAuth;
import fr.univtln.bruno.samples.jaxrs.security.annotations.JWTAuth;
import fr.univtln.bruno.samples.jaxrs.security.filter.request.BasicAuthenticationFilter;
import fr.univtln.bruno.samples.jaxrs.security.filter.request.JsonWebTokenFilter;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import lombok.extern.java.Log;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * A administration class for the libraryBiblio resource.
 * A demo JAXRS class, that manages authors and offers a secured access.
 */
@Log
// The Java class will be hosted at the URI path "/biblio"
@Path("setup")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
public class AdminResource {

    /**
     * A GET method to access the context of the request : The URI, the HTTP headers, the request and the security context (needs authentication see below).
     *
     * @param uriInfo         the uri info
     * @param httpHeaders     the http headers
     * @param request         the request
     * @param securityContext the security context
     * @return A string representation of the available data.
     */
    @GET
    @Path("context")
    public String getContext(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @Context Request request, @Context SecurityContext securityContext) {
        String result = "UriInfo: (" + uriInfo.getRequestUri().toString() + ")\n"
                        + "Method: (" + request.getMethod() + ")\n"
                        + "HttpHeaders(" + httpHeaders.getRequestHeaders().toString() + ")\n";

        if (securityContext != null) {
            result += " SecurityContext(Auth.scheme: [" + securityContext.getAuthenticationScheme() + "] \n";
            if (securityContext.getUserPrincipal() != null)
                result += "    user: [" + securityContext.getUserPrincipal().getName() + "] \n";
            result += "    secured: [" + securityContext.isSecure() + "] )";
        }
        return result;
    }

    /**
     * A GET restricted to ADMIN role with basic authentication.
     *
     * @param securityContext the security context
     * @return the restricted to admins
     * @see BasicAuthenticationFilter
     */
    @GET
    @Path("adminsonly")
    @RolesAllowed("ADMIN")
    @BasicAuth
    public String getRestrictedToAdmins(@Context SecurityContext securityContext) {
        return "secret for admins !" + securityContext.getUserPrincipal().getName();
    }

    /**
     * A GET restricted to USER role with basic authentication (and not ADMIN !).
     *
     * @param securityContext the security context
     * @return the restricted to users
     * @see BasicAuthenticationFilter
     */
    @GET
    @Path("usersonly")
    @RolesAllowed("USER")
    @BasicAuth
    public String getRestrictedToUsers(@Context SecurityContext securityContext) {
        return "secret for users ! to " + securityContext.getUserPrincipal().getName();
    }

    /**
     * A GET restricted to USER & ADMIN roles, secured with a JWT Token.
     *
     * @param securityContext the security context
     * @return the string
     * @see JsonWebTokenFilter
     */
    @GET
    @Path("secured")
    @RolesAllowed({"USER", "ADMIN"})
    @JWTAuth
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public String securedByJWT(@Context SecurityContext securityContext) {
        log.info("USER ACCESS :" + securityContext.getUserPrincipal().getName());
        return "Access with JWT ok for " + securityContext.getUserPrincipal().getName();
    }

    /**
     * A GET restricted to ADMIN roles, secured with a JWT Token.
     *
     * @param securityContext the security context
     * @return the string
     */
    @GET
    @Path("secured/admin")
    @RolesAllowed({"ADMIN"})
    @JWTAuth
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public String securedByJWTAdminOnly(@Context SecurityContext securityContext) {
        log.info("ADMIN ACCESS :" + securityContext.getUserPrincipal().getName());
        return "Access with JWT ok for " + securityContext.getUserPrincipal().getName();
    }

    /**
     * a GET method to obtain a JWT token with basic authentication for USER and ADMIN roles.
     *
     * @param securityContext the security context
     * @return the base64 encoded JWT Token.
     */
    @GET
    @Path("login")
    @RolesAllowed({"USER", "ADMIN"})
    @BasicAuth
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public String login(@Context SecurityContext securityContext) {
        if (securityContext.isSecure() && securityContext.getUserPrincipal() instanceof User) {
            User user = (User) securityContext.getUserPrincipal();
            return Jwts.builder()
                    .issuer("sample-jaxrs")
                    .issuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .subject(user.getEmail())
                    .claim("firstname", user.getFirstName())
                    .claim("lastname", user.getLastName())
                    .claim("roles", user.getRoles())
                    .expiration(Date.from(LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant()))
                    .notBefore(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .signWith(InMemoryLoginModule.KEY)
                    .compact();
        }
        throw new WebApplicationException(new AuthenticationException());
    }
}
