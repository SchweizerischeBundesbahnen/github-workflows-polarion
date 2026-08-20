package ch.sbb.polarion.extension.example.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;

// /api/* path — @Secured required by design (AuthenticationFilter validates
// Bearer PAT or X-Polarion-REST-Token). Without it, anyone reaching the
// endpoint from outside the Polarion session can mutate state.
@Path("/api/projects/{project}/keys")
public class ProjectKeysApiVulnerable {

    // ruleid: polarion-rest-no-authz-check
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

    // ruleid: polarion-rest-no-authz-check
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

    // ruleid: polarion-rest-no-authz-check
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

// Ambiguous path (no class-level @Path → defaults to extension root, neither
// /internal nor /api by convention). Rule still fires; reviewer must confirm
// the URL space.
class ProjectKeysAmbiguous {

    // ruleid: polarion-rest-no-authz-check
    @POST
    @Path("/raw/{project}/{key}")
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
}
