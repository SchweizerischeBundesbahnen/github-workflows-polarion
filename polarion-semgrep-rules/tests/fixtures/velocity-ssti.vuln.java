package ch.sbb.polarion.extension.example.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.StringWriter;

public class VelocitySstiVulnerable {

    // ruleid: polarion-velocity-ssti
    public String renderDefault(String userTemplate, VelocityContext ctx) {
        VelocityEngine engine = new VelocityEngine();
        engine.init();
        StringWriter writer = new StringWriter();
        engine.evaluate(ctx, writer, "user-template", userTemplate);
        return writer.toString();
    }

    // known-miss: polarion-velocity-ssti — `return new VelocityEngine(...)` is
    // not a bare statement, so the method-scoped pattern does not reach it. See
    // "Known rule gaps" in the pack README.
    public VelocityEngine buildEngine() {
        // Even with custom Properties, missing SecureUberspector is unsafe.
        java.util.Properties props = new java.util.Properties();
        props.setProperty("input.encoding", "UTF-8");
        return new VelocityEngine(props);
    }
}
