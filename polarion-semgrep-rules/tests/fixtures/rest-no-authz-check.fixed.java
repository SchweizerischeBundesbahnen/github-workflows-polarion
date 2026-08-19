package ch.sbb.polarion.extension.example.rest;

import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.platform.security.ISecurityService;

// /internal/* path — @Secured intentionally absent. Container-level
// <security-constraint role-name="user"> + Polarion FORM login authenticates;
// DoAsFilter establishes the session Subject; platform APIs self-check
// permissions. SameSite=Lax + state-change-on-non-GET handles CSRF. This is
// the documented pattern in the generic extension's README.
@Path("/internal/projects/{project}/keys")
public class ProjectKeysInternal {

    // ok: polarion-rest-no-authz-check — /internal/* is by design
    @POST
    @Path("/{key}")
    public Response createKey(@PathParam("project") String project,
                              @PathParam("key") String key,
                              String value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project)
                       .customFields().setValue(key, value);
            return null;
        });
        return Response.ok().build();
    }

    // ok: polarion-rest-no-authz-check — /internal/* is by design
    @PUT
    @Path("/{key}")
    public Response updateKey(@PathParam("project") String project,
                              @PathParam("key") String key,
                              String value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project)
                       .customFields().setValue(key, value);
            return null;
        });
        return Response.ok().build();
    }

    // ok: polarion-rest-no-authz-check — /internal/* is by design
    @DELETE
    @Path("/{key}")
    public Response deleteKey(@PathParam("project") String project,
                              @PathParam("key") String key) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project)
                       .customFields().delete(key);
            return null;
        });
        return Response.noContent().build();
    }
}

// Class-level @Secured — /api/* with the proper authentication filter.
// Mirrors the *ApiController pattern in the generic extension and api-extender.
@Secured
@Path("/api/projects/{project}/keys")
class ProjectKeysApi {

    // ok: polarion-rest-no-authz-check — class-level @Secured
    @POST
    @Path("/{key}")
    public Response createKey(@PathParam("project") String project,
                              @PathParam("key") String key,
                              String value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project).customFields().setValue(key, value);
            return null;
        });
        return Response.ok().build();
    }

    // ok: polarion-rest-no-authz-check — class-level @Secured
    @DELETE
    @Path("/{key}")
    public Response deleteKey(@PathParam("project") String project,
                              @PathParam("key") String key) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project).customFields().delete(key);
            return null;
        });
        return Response.noContent().build();
    }
}

// Method-level @Secured — only the annotated method is exempt. Hybrid class
// where some methods are @Secured and others are not is unusual; if the path
// is ambiguous (not /internal), the unannotated methods would still flag.
@Path("/api/mixed/{project}")
class ProjectKeysApiMethodSecured {

    // ok: polarion-rest-no-authz-check — method-level @Secured
    @Secured
    @POST
    @Path("/{key}")
    public Response createKey(@PathParam("project") String project,
                              @PathParam("key") String key,
                              String value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project).customFields().setValue(key, value);
            return null;
        });
        return Response.ok().build();
    }
}

// Ambiguous path with explicit @RolesAllowed — Jakarta Security exempts.
@Path("/some-extension/keys")
class ProjectKeysJakartaRoles {

    private final ISecurityService securityService;

    public ProjectKeysJakartaRoles(ISecurityService securityService) {
        this.securityService = securityService;
    }

    // ok: polarion-rest-no-authz-check — @RolesAllowed
    @RolesAllowed({"project_admin"})
    @PUT
    @Path("/{key}")
    public Response updateKey(@PathParam("project") String project,
                              @PathParam("key") String key,
                              String value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project)
                       .customFields().setValue(key, value);
            return null;
        });
        return Response.ok().build();
    }

    // ok: polarion-rest-no-authz-check — explicit hasPermission early return
    @DELETE
    @Path("/{key}")
    public Response deleteKey(@Context SecurityContext sc,
                              @PathParam("project") String project,
                              @PathParam("key") String key) {
        String user = sc.getUserPrincipal().getName();
        if (!securityService.hasPermission(user, "DELETE", project)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project)
                       .customFields().delete(key);
            return null;
        });
        return Response.noContent().build();
    }
}
