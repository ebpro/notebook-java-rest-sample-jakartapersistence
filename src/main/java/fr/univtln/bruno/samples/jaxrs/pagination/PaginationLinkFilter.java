package fr.univtln.bruno.samples.jaxrs.pagination;

import fr.univtln.bruno.samples.jaxrs.model.Page;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Provider
@Log
public class PaginationLinkFilter implements ContainerResponseFilter {
    public static final String JAXRS_SAMPLE_TOTAL_COUNT = "JAXRS_Sample-Total-Count";
    public static final String JAXRS_SAMPLE_PAGE_COUNT = "JAXRS_Sample-Page-Count";
    public static final String PREV_REL = "previous";
    public static final String NEXT_REL = "next";
    public static final String FIRST_REL = "first";
    public static final String LAST_REL = "last";
    public static final String PAGE_QUERY_PARAM = "page";
    public static final int FIRST_PAGE = 1;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        //If the entity in the response is not a Page, we stop here
        if (!(responseContext.getEntity() instanceof Page)) {
            return;
        }

        UriInfo uriInfo = requestContext.getUriInfo();
        Page entity = (Page) responseContext.getEntity();

        if (entity.getPageNumber() > entity.getPageTotal())
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        //We replace the entity by the content of the page (we remove the envelope).
        responseContext.setEntity(entity.getContent());

        List<Link> linksList = new ArrayList<>();

        //We add the need semantic links in the header
        //Not on the first page
        if (entity.getPageNumber() > FIRST_PAGE) {
            linksList.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam(PAGE_QUERY_PARAM,
                            entity.getPageNumber() - 1))
                    .rel(PREV_REL)
                    .build());
            linksList.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam(PAGE_QUERY_PARAM,
                            1))
                    .rel(FIRST_REL)
                    .build());
        }
        //Not on the last
        if (entity.getPageNumber() < entity.getPageTotal()) {
            linksList.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam(PAGE_QUERY_PARAM,
                            entity.getPageNumber() + 1))
                    .rel(NEXT_REL)
                    .build());
            linksList.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam(PAGE_QUERY_PARAM,
                            entity.getPageTotal()))
                    .rel(LAST_REL)
                    .build());
        }

        if (!linksList.isEmpty())
            responseContext.getHeaders().add("Link", linksList.stream().map(Link::toString).collect(Collectors.joining(",")));
        //We add pagination metadata in the header
        responseContext.getHeaders().add(JAXRS_SAMPLE_TOTAL_COUNT, entity.getElementTotal());
        responseContext.getHeaders().add(JAXRS_SAMPLE_PAGE_COUNT, entity.getPageTotal());
    }
}
