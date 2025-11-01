package org.syve.filters;

import java.io.IOException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
@RegisterForReflection
public class AuthFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "security.api-token")
    String apiToken;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        if (!path.matches(".*/upload$")) {
            return;
        }

        String header = requestContext.getHeaderString("Authorization");

        if (header == null || !header.equals("Bearer " + apiToken)) {
            requestContext.abortWith(
                    jakarta.ws.rs.core.Response
                            .status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED)
                            .entity("Unauthorized: invalid or missing token")
                            .build());
        }
    }
}
