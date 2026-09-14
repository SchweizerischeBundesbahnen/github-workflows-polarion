package ch.sbb.polarion.extension.example;

import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.platform.security.ISecurityService;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.apache.commons.io.FileUtils;

public class TransactionVulnerable {

    private final ISecurityService securityService;

    public TransactionVulnerable(ISecurityService securityService) {
        this.securityService = securityService;
    }

    public void jdbcUpdate(Connection connection, String sql) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // ruleid: polarion-transaction-no-permission-check
                statement.executeUpdate();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void jdbcExecute(Connection connection, String sql) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                Statement statement = connection.createStatement();
                // ruleid: polarion-transaction-no-permission-check
                statement.execute(sql);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void nioWrite(Path path, String content) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.writeString(path, content);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void ioStream(String fileName, byte[] content) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // ruleid: polarion-transaction-no-permission-check
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                out.write(content);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void ioDelete(String fileName) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            File file = new File(fileName);
            // ruleid: polarion-transaction-no-permission-check
            file.delete();
            return null;
        });
    }

    public void commonsIoWrite(File file, String content) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                FileUtils.writeStringToFile(file, content, "UTF-8");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void reflectionIntoInternals(Object target, String fieldName, Object value) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                // ruleid: polarion-transaction-no-permission-check
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A check placed after the change cannot protect it.
    public void checkAfterChange(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            securityService.checkPermission(user, "MODIFY", resource);
            return null;
        });
    }

    // A comment is not a check: securityService.checkPermission(user, ...)
    public void commentNamingCheck(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // TODO securityService.checkPermission(user, "MODIFY", resource);
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A check in a sibling method does not protect this one.
    public void siblingChecks(String user, Object resource) {
        securityService.checkPermission(user, "MODIFY", resource);
    }

    public void siblingUnchecked(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.deleteIfExists(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // semgrep does not follow the call into the helper.
    public void bypassInHelper(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // known-miss: polarion-transaction-no-permission-check
            deleteQuietly(path);
            return null;
        });
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // ignored
        }
    }

    // A chained statement has no declared type for `execute(...)` to bind.
    public void jdbcChainedExecute(Connection connection, String sql) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // known-miss: polarion-transaction-no-permission-check
                connection.prepareStatement(sql).execute();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }
}
