package ch.sbb.polarion.extension.example;

import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.security.ISecurityService;
import com.polarion.platform.security.PermissionDeniedException;

public class TransactionFixed {

    private final ISecurityService securityService;

    public TransactionFixed(ISecurityService securityService) {
        this.securityService = securityService;
    }

    // ok: polarion-transaction-no-permission-check
    public void mutateWithCheck(String currentUser, String workItemId, String newTitle) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IWorkItem wi = transaction.workItems().getById(workItemId);
            securityService.checkPermission(currentUser, "MODIFY", wi);
            wi.setTitle(newTitle);
            wi.save();
            return null;
        });
    }

    // ok: polarion-transaction-no-permission-check
    public void deleteWithCheck(String currentUser, String workItemId) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IWorkItem wi = transaction.workItems().getById(workItemId);
            if (!securityService.hasPermission(currentUser, "DELETE", wi)) {
                throw new PermissionDeniedException("not allowed");
            }
            wi.delete();
            return null;
        });
    }
}
