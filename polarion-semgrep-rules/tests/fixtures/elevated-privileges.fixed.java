package ch.sbb.polarion.extension.example;

import ch.sbb.polarion.extension.generic.rest.filter.RequestContextUtil;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.platform.security.ISecurityService;
import java.security.PrivilegedAction;

public class ElevatedPrivilegesFixed {

    private final ISecurityService securityService;
    private final PolarionService polarionService;

    public ElevatedPrivilegesFixed(ISecurityService securityService, PolarionService polarionService) {
        this.securityService = securityService;
        this.polarionService = polarionService;
    }

    // ok: polarion-elevated-privileges
    public void runAsRequestUser(PrivilegedAction<Void> action) {
        securityService.doAsUser(RequestContextUtil.getUserSubject(), action);
    }

    // callPrivileged runs as the request's user, despite its name.
    // ok: polarion-elevated-privileges
    public void runThroughCallPrivileged(Runnable action) {
        polarionService.callPrivileged(action);
    }

    // ok: polarion-elevated-privileges
    public String currentUser() {
        return securityService.getCurrentUser();
    }

    // The no-argument login re-authenticates the request user, it elevates
    // nothing. This is the generic PersonalAccessTokenValidator spelling.
    // ok: polarion-elevated-privileges
    public Object reauthenticateRequestUser() {
        return securityService.login();
    }
}
