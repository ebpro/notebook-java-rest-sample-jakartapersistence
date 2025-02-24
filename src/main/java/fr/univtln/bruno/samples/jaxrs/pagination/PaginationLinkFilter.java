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

/**
 * Filter that handles pagination metadata and HATEOAS links for REST responses.
 * Implements RFC 5988 Web Linking standard.
 */
@Provider
@Log
public class PaginationLinkFilter implements ContainerResponseFilter {
    private static final String JAXRS_SAMPLE_TOTAL_COUNT = "X-Total-Count";
    private static final String JAXRS_SAMPLE_PAGE_COUNT = "X-Page-Count";
    private static final String PREV_REL = "prev";
    private static final String NEXT_REL = "next";
    private static final String FIRST_REL = "first";
    private static final String LAST_REL = "last";
    private static final String PAGE_QUERY_PARAM = "page";
    private static final int FIRST_PAGE = 1;

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) {
        if (!isPageResponse(responseContext)) {
            return;
        }

        Page<?> entity = (Page<?>) responseContext.getEntity();
        validatePageNumber(entity);

        responseContext.setEntity(entity.getContent());
        addPaginationHeaders(responseContext, entity);
        addNavigationLinks(requestContext.getUriInfo(), responseContext, entity);
    }

    private boolean isPageResponse(ContainerResponseContext responseContext) {
        return responseContext.getEntity() instanceof Page;
    }

    private void validatePageNumber(Page<?> entity) {
        if (entity.getPageNumber() > entity.getPageTotal()) {
            throw new WebApplicationException(
                String.format("Page %d exceeds total pages %d", 
                    entity.getPageNumber(), entity.getPageTotal()),
                Response.Status.NOT_FOUND);
        }
    }

    private void addPaginationHeaders(ContainerResponseContext responseContext, Page<?> entity) {
        responseContext.getHeaders().add(JAXRS_SAMPLE_TOTAL_COUNT, entity.getElementTotal());
        responseContext.getHeaders().add(JAXRS_SAMPLE_PAGE_COUNT, entity.getPageTotal());
    }

    private void addNavigationLinks(UriInfo uriInfo, 
                                  ContainerResponseContext responseContext, 
                                  Page<?> entity) {
        List<Link> links = new ArrayList<>();

        addPreviousAndFirstLinks(links, uriInfo, entity);
        addNextAndLastLinks(links, uriInfo, entity);

        if (!links.isEmpty()) {
            responseContext.getHeaders().add("Link", 
                links.stream()
                    .map(Link::toString)
                    .collect(Collectors.joining(", ")));
        }
    }

    private void addPreviousAndFirstLinks(List<Link> links, UriInfo uriInfo, Page<?> entity) {
        if (entity.getPageNumber() > FIRST_PAGE) {
            links.add(createLink(uriInfo, PREV_REL, entity.getPageNumber() - 1));
            links.add(createLink(uriInfo, FIRST_REL, FIRST_PAGE));
        }
    }

    private void addNextAndLastLinks(List<Link> links, UriInfo uriInfo, Page<?> entity) {
        if (entity.getPageNumber() < entity.getPageNumber()) {
            links.add(createLink(uriInfo, NEXT_REL, entity.getPageNumber() + 1));
            links.add(createLink(uriInfo, LAST_REL, entity.getPageTotal()));
        }
    }

    private Link createLink(UriInfo uriInfo, String rel, long pageNumber) {
        return Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                .replaceQueryParam(PAGE_QUERY_PARAM, pageNumber))
                .rel(rel)
                .build();
    }
}