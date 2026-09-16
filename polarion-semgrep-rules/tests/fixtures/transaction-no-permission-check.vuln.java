package ch.sbb.polarion.extension.example;

import static com.polarion.alm.shared.api.transaction.TransactionalExecutor.executeInWriteTransaction;

import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.platform.security.ISecurityService;
import com.polarion.platform.security.PermissionDeniedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    // semgrep resolves the static import, so the bare call is reached.
    public void staticImport(Path path) {
        executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A guard that throws a non-permission type does not clear the rule. A
    // documented false positive: the clause names the thrown type, because a
    // bare `throw` matched a rethrow nested in a catch.
    public void guardThrowsOtherType(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                throw new IllegalStateException("not allowed");
            }
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A check wrapped in an extension helper with another name is not
    // recognized, neither as a statement nor as a guard. A documented false
    // positive.
    public void checkInRenamedHelper(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            checkPermissions(user, resource);
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void guardInRenamedHelper(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!isModificationAllowed(user, resource)) {
                throw new PermissionDeniedException("not allowed");
            }
            // ruleid: polarion-transaction-no-permission-check
            file.delete();
            return null;
        });
    }

    private void checkPermissions(String user, Object resource) {
        securityService.checkPermission(user, "MODIFY", resource);
    }

    private boolean isModificationAllowed(String user, Object resource) {
        return securityService.hasPermission(user, "MODIFY", resource);
    }

    // A braceless positive guard covers its own statement only.
    public void bracelessGuardThenWrite(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (securityService.hasPermission(user, "MODIFY", resource)) System.err.println("allowed");
            // ruleid: polarion-transaction-no-permission-check
            file.delete();
            return null;
        });
    }

    // The accepted exception types are matched by simple name, so an inline
    // fully qualified name does not clear the rule. A documented false positive.
    public void fullyQualifiedForbidden(String user, File file, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                throw new jakarta.ws.rs.ForbiddenException("not allowed");
            }
            // ruleid: polarion-transaction-no-permission-check
            file.delete();
            return null;
        });
    }

    // The write happens precisely when permission is denied. The rethrow in
    // the nested catch must not make the guard look terminating.
    public void invertedGuardWrites(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                try {
                    // ruleid: polarion-transaction-no-permission-check
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return null;
        });
    }

    // A guard whose branch does not stop execution protects nothing.
    public void guardDoesNotStop(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                System.err.println("denied");
            }
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // The positive guard clause covers the whole if statement, else included.
    public void writeInElseOfGuard(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (securityService.hasPermission(user, "MODIFY", resource)) {
                System.err.println("allowed");
            } else {
                try {
                    // known-miss: polarion-transaction-no-permission-check
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return null;
        });
    }

    // The terminating guard clause covers its own body as well as what follows.
    public void writeInsideTerminatingGuard(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                try {
                    // known-miss: polarion-transaction-no-permission-check
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                throw new PermissionDeniedException("not allowed");
            }
            return null;
        });
    }

    // The statement ellipsis descends into the nested if, so a throw on one
    // path only is taken as a terminating guard. Semgrep matches statements,
    // not paths.
    public void throwOnOnePathOnly(String user, Path path, Object resource, boolean strict) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            if (!securityService.hasPermission(user, "MODIFY", resource)) {
                if (strict) {
                    throw new PermissionDeniedException("not allowed");
                }
            }
            try {
                // known-miss: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A method of the same class that the lambda calls is followed one level.
    // The finding lands in the helper.
    public void bypassInHelper(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            deleteQuietly(path);
            return null;
        });
    }

    private void deleteQuietly(Path path) {
        try {
            // ruleid: polarion-transaction-no-permission-check
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // ignored
        }
    }

    public String bypassInThisHelper(Path path) {
        return TransactionalExecutor.executeInWriteTransaction(transaction -> this.writeMarker(path));
    }

    private String writeMarker(Path path) {
        try {
            // ruleid: polarion-transaction-no-permission-check
            Files.writeString(path, "marker");
        } catch (Exception e) {
            // ignored
        }
        return "ok";
    }

    // The check is looked up from the finding, so a check in the caller does
    // not clear a finding in the helper. A documented false positive.
    public void helperAfterCallerCheck(String user, Path path, Object resource) {
        securityService.checkPermission(user, "MODIFY", resource);
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            removeChecked(path);
            return null;
        });
    }

    private void removeChecked(Path path) {
        try {
            // ruleid: polarion-transaction-no-permission-check
            Files.delete(path);
        } catch (Exception e) {
            // ignored
        }
    }

    // Only one level of call is followed.
    public void bypassTwoLevelsDown(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            firstLevel(path);
            return null;
        });
    }

    private void firstLevel(Path path) {
        secondLevel(path);
    }

    private void secondLevel(Path path) {
        try {
            // known-miss: polarion-transaction-no-permission-check
            Files.delete(path);
        } catch (Exception e) {
            // ignored
        }
    }

    // A method on another object usually lives in another file, which semgrep
    // does not read.
    public void bypassInOtherObject(TemporaryStore store, Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // known-miss: polarion-transaction-no-permission-check
            store.remove(path);
            return null;
        });
    }

    // A method reference is not followed.
    public void bypassInMethodReference() {
        TransactionalExecutor.executeInWriteTransaction(this::purge);
    }

    private Object purge(Object transaction) {
        try {
            // known-miss: polarion-transaction-no-permission-check
            Files.delete(Path.of("purge"));
        } catch (Exception e) {
            // ignored
        }
        return null;
    }

    // A check called without a receiver is not recognized, even when it
    // delegates to the platform check. The `this.`-qualified and statically
    // imported spellings are recognized; both sit in the fixed fixture.
    // A documented false positive.
    public void receiverlessCheck(String user, Path path, Object resource) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            checkPermission(user, resource);
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    private void checkPermission(String user, Object resource) {
        securityService.checkPermission(user, "MODIFY", resource);
    }

    // The helper branch matches on name and arity, and the enclosing class
    // pattern is satisfied by the outer class, so a helper of the same name
    // and arity in a NESTED class is reported although the transaction cannot
    // reach it. A documented false positive.
    public void callsOuterHelper(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            purgeTemporary(path);
            return null;
        });
    }

    private void purgeTemporary(Path path) {
        try {
            // ruleid: polarion-transaction-no-permission-check
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // ignored
        }
    }

    static class NestedStore {
        private void purgeTemporary(Path path) {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.delete(path);
            } catch (Exception e) {
                // ignored
            }
        }
    }

    // The helper branch is spelled out for 0 to 3 parameters, so a helper with
    // four is not followed.
    public void helperWithFourParameters(Path path, String first, String second, String third) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            writeAll(path, first, second, third);
            return null;
        });
    }

    private void writeAll(Path path, String first, String second, String third) {
        try {
            // known-miss: polarion-transaction-no-permission-check
            Files.writeString(path, first + second + third);
        } catch (Exception e) {
            // ignored
        }
    }

    // PrintWriter and PrintStream reach a file when constructed from a file
    // name or a File.
    public void printWriterToFileName(String fileName) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // ruleid: polarion-transaction-no-permission-check
            try (PrintWriter writer = new PrintWriter(fileName)) {
                writer.println("entry");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void printStreamToFile(File file) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            // ruleid: polarion-transaction-no-permission-check
            try (PrintStream stream = new PrintStream(file)) {
                stream.println("entry");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    public void commonsOpenOutputStream(File file) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                FileUtils.openOutputStream(file).close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // newByteChannel opens for reading unless a write option is passed.
    public void byteChannelForWriting(Path path) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                Files.newByteChannel(path, StandardOpenOption.WRITE).close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    // A chained statement is matched through the Connection call that creates it.
    public void jdbcChainedExecute(Connection connection, String sql) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            try {
                // ruleid: polarion-transaction-no-permission-check
                connection.prepareStatement(sql).execute();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }
}
