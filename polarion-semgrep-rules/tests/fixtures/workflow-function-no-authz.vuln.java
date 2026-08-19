package ch.sbb.polarion.extension.example.workflow;

import com.polarion.alm.tracker.workflow.IFunction;
import com.polarion.alm.tracker.workflow.IArguments;
import com.polarion.alm.tracker.workflow.IActionContext;
import com.polarion.alm.tracker.model.IWorkItem;

public class WorkflowFunctionVulnerable implements IFunction<IWorkItem> {

    // ruleid: polarion-workflow-function-no-authz
    @Override
    public Object execute(IArguments arguments, IActionContext actionContext) {
        IWorkItem wi = (IWorkItem) actionContext.getTarget();
        // Reads a target field name from workflow.xml-supplied arguments and
        // overwrites it. No invoking-user check, no permission validation.
        String fieldName = arguments.getAsString("targetField");
        String newValue = arguments.getAsString("newValue");
        wi.setValue(fieldName, newValue);
        wi.save();
        return null;
    }
}
