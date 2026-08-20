package ch.sbb.polarion.extension.example.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;

@Path("/internal/projects/{project}/keys")
public class GetWithWriteTransactionFixed {

    // ok: polarion-get-with-write-transaction — read-only transaction
    @GET
    @Path("/{key}")
    public Response readKey(@PathParam("project") String project,
                            @PathParam("key") String key) {
        String value = TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction ->
            transaction.projects().getById(project).customFields().getValue(key)
        );
        return Response.ok(value).build();
    }

    // ok: polarion-get-with-write-transaction — pure GET, no transaction at all
    @GET
    @Path("/{key}/length")
    public Response keyLength(@PathParam("key") String key) {
        return Response.ok(String.valueOf(key.length())).build();
    }

    // ok: polarion-get-with-write-transaction — write transaction is fine on POST
    @POST
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
}
