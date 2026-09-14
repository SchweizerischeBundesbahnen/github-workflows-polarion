package ch.sbb.polarion.extension.example;

import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.security.ISecurityService;
import com.polarion.platform.security.PermissionDeniedException;
import com.polarion.platform.service.repository.IRepositoryConnection;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import jakarta.ws.rs.ForbiddenException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class TransactionFixed {

    private final ISecurityService securityService;
    private final IRepositoryService repositoryService;

    public TransactionFixed(ISecurityService securityService, IRepositoryService repositoryService) {
        this.securityService = securityService;
        this.repositoryService = repositoryService;
    }

    // IPObject mutators check the active Subject themselves.
    // ok: polarion-transaction-no-permission-check
    public void platformMutation(String workItemId, String newTitle) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IWorkItem wi = transaction.workItems().getById(workItemId);
            wi.setTitle(newTitle);
            wi.save();
            return null;
        });
    }

    // IRepositoryConnection checks the active Subject itself. This is the
    // api-extender GenericFields.save() shape.
    // ok: polarion-transaction-no-permission-check
    public void repositoryWrite(ILocation location, String content) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IRepositoryConnection connection = repositoryService.getConnection(location);
            connection.setContent(location, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void checkInsideTransaction(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            securityService.checkPermission(user, "MODIFY", resource);
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void checkBeforeTransaction(String user, Path path, Object resource) {
        securityService.checkPermission(user, "MODIFY", resource);
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardClause(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                throw new PermissionDeniedException("not allowed");
            }
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardClauseForbidden(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                throw new ForbiddenException("not allowed");
            }
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardClauseWithoutBraces(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) throw new PermissionDeniedException("not allowed");
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public Object guardClauseReturns(String user, Path path, Object resource) {
        return TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                return null;
            }
            try {
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return path;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardReturnsValueWithoutBraces(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) return null;
            file.delete();
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardReturnsWithoutBraces(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            runNow(() -> {
                if (!securityService.hasPermission(user, "MODIFY", resource)) return;
                file.delete();
            });
            return null;
        });
    }

    private void runNow(Runnable runnable) {
        runnable.run();
    }

    // The braced positive clause also reaches a braceless body.
    // ok: polarion-transaction-no-permission-check
    public void guardedStatementWithoutBraces(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (securityService.hasPermission(user, "MODIFY", resource)) file.delete();
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void guardedBranch(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (securityService.hasPermission(user, "MODIFY", resource)) {
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return null;
        });
    }

    // Reads are not changes.
    // ok: polarion-transaction-no-permission-check
    public String readInsideTransaction(Path path) {
        return TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                return Files.exists(path) ? Files.readString(path) : null;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    // File IO outside a write transaction is routine: export logs, temporary
    // files. This is the pdf-exporter HtmlLogger shape.
    // ok: polarion-transaction-no-permission-check
    public void writeOutsideTransaction(Path path, String content) throws Exception {
        Files.writeString(path, content);
    }

    // The helper branch follows write transactions only.
    // ok: polarion-transaction-no-permission-check
    public String helperFromReadOnlyTransaction(Path path) {
        return TransactionalExecutor.executeInReadOnlyTransaction(transaction -> cleanTemporary(path));
    }

    private String cleanTemporary(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // ignored
        }
        return null;
    }

    // A chained query is a read.
    // ok: polarion-transaction-no-permission-check
    public void jdbcChainedQuery(Connection connection, String sql) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                connection.prepareStatement(sql).executeQuery();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }
}
