package ch.sbb.polarion.extension.example.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.StringWriter;

public class VelocitySstiVulnerable {

    // A field initializer has no enclosing method, so the suppression has to
    // scope to the class for this shape to be reachable at all.
    // ruleid: polarion-velocity-ssti
    private static final VelocityEngine SHARED_ENGINE = new VelocityEngine();

    private final VelocityEngine perInstanceEngine;

    public VelocitySstiVulnerable() {
        // A constructor has no return type for a `$RET $METHOD(...)` pattern to
        // bind, so it is reached through the same class-scoped branch.
        // ruleid: polarion-velocity-ssti
        this.perInstanceEngine = new VelocityEngine();
    }

    public String renderDefault(String userTemplate, VelocityContext ctx) {
        // ruleid: polarion-velocity-ssti
        VelocityEngine engine = new VelocityEngine();
        engine.init();
        StringWriter writer = new StringWriter();
        engine.evaluate(ctx, writer, "user-template", userTemplate);
        return writer.toString();
    }

    public VelocityEngine buildEngine() {
        // A comment naming SecureUberspector must not suppress the finding. The
        // earlier suppression was a `pattern-not-regex` over the matched region,
        // which matched this comment as text and silenced the case — which is
        // why it was mis-recorded as a shape the pattern could not reach.
        java.util.Properties props = new java.util.Properties();
        props.setProperty("input.encoding", "UTF-8");
        // ruleid: polarion-velocity-ssti
        return new VelocityEngine(props);
    }
}

// A method that hardens its own engine must not clear an unhardened sibling.
class VelocitySstiPartiallyFixed {

    public VelocityEngine buildSecure() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        return new VelocityEngine(props);
    }

    public VelocityEngine buildInsecure() {
        // ruleid: polarion-velocity-ssti
        return new VelocityEngine();
    }
}
