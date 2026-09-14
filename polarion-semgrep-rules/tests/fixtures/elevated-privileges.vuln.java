package ch.sbb.polarion.extension.example;

import com.polarion.alm.tracker.model.IComment;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.security.ISecurityService;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;

public class ElevatedPrivilegesVulnerable {

    private final ISecurityService securityService;

    public ElevatedPrivilegesVulnerable(ISecurityService securityService) {
        this.securityService = securityService;
    }

    public void setAuthorAsSystemUser(IComment comment, Object author) {
        // ruleid: polarion-elevated-privileges
        securityService.doAsSystemUser((PrivilegedAction<Void>) () -> {
            comment.setValue("author", author);
            return null;
        });
    }

    // The receiver type of a chained lookup cannot be inferred, which is why
    // the rule does not type it.
    public Object readAsSystemUser() {
        // ruleid: polarion-elevated-privileges
        return PlatformContext.getPlatform().lookupService(ISecurityService.class).doAsSystemUser(
                (PrivilegedAction<Object>) () -> null);
    }

    public void runAsStoredSystemSubject(PrivilegedAction<Void> action) {
        // ruleid: polarion-elevated-privileges
        Subject system = securityService.getSystemUserSubject();
        securityService.doAsUser(system, action);
    }

    public Subject loginTechnicalAccount() throws Exception {
        // ruleid: polarion-elevated-privileges
        return securityService.loginUserFromVault("technical-account", null);
    }
}
