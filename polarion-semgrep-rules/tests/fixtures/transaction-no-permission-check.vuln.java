package ch.sbb.polarion.extension.example;

import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.tracker.model.IWorkItem;

public class TransactionVulnerable {

    // ruleid: polarion-transaction-no-permission-check
    public void mutateWithoutCheck(String workItemId, String newTitle) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IWorkItem wi = transaction.workItems().getById(workItemId);
            wi.setTitle(newTitle);
            wi.save();
            return null;
        });
    }

    // ruleid: polarion-transaction-no-permission-check
    public void deleteWithoutCheck(String workItemId) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            transaction.workItems().getById(workItemId).delete();
            return null;
        });
    }
}
