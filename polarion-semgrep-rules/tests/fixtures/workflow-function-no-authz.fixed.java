package ch.sbb.polarion.extension.example.workflow;

import com.polarion.alm.tracker.workflow.IFunction;
import com.polarion.alm.tracker.workflow.IArguments;
import com.polarion.alm.tracker.workflow.IActionContext;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.platform.security.ISecurityService;

public class WorkflowFunctionFixed implements IFunction<IWorkItem> {

    private final ISecurityService securityService;

    public WorkflowFunctionFixed(ISecurityService securityService) {
        this.securityService = securityService;
    }

    // ok: polarion-workflow-function-no-authz — checks invoking user
    @Override
    public Object execute(IArguments arguments, IActionContext actionContext) {
        IWorkItem wi = (IWorkItem) actionContext.getTarget();
        String currentUser = actionContext.getCurrentUser();
        String fieldName = arguments.getAsString("targetField");
        String newValue = arguments.getAsString("newValue");
        if (!securityService.hasPermission(currentUser, "MODIFY", wi)) {
            throw new SecurityException("user " + currentUser + " not allowed to modify " + fieldName);
        }
        wi.setValue(fieldName, newValue);
        wi.save();
        return null;
    }
}
