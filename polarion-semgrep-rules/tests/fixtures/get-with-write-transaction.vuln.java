package ch.sbb.polarion.extension.example.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;

@Path("/internal/projects/{project}/keys")
public class GetWithWriteTransactionVulnerable {

    // ruleid: polarion-get-with-write-transaction
    // Hypothetical: a developer wrote a "convenience" GET that bumps a counter
    // and returns the new value. Looks innocent; one-click CSRF via <img>.
    @GET
    @Path("/{key}/increment")
    public Response incrementKey(@PathParam("project") String project,
                                 @PathParam("key") String key) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            var p = transaction.projects().getById(project);
            int current = Integer.parseInt(p.customFields().getValue(key));
            p.customFields().setValue(key, String.valueOf(current + 1));
            return null;
        });
        return Response.ok().build();
    }

    // ruleid: polarion-get-with-write-transaction
    // Same pattern via an injected executor (the rule's $EXEC arm).
    private final TransactionalExecutor exec;

    public GetWithWriteTransactionVulnerable(TransactionalExecutor exec) {
        this.exec = exec;
    }

    @GET
    @Path("/{key}/touch")
    public Response touchKey(@PathParam("project") String project,
                             @PathParam("key") String key) {
        exec.executeInWriteTransaction(transaction -> {
            transaction.projects().getById(project).customFields().setValue(key, "touched");
            return null;
        });
        return Response.ok().build();
    }
}
