package ch.sbb.polarion.extension.example.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.StringWriter;
import java.util.Properties;

public class VelocitySstiFixed {

    // ok: polarion-velocity-ssti — SecureUberspector configured
    public VelocityEngine buildEngine() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        return new VelocityEngine(props);
    }

    // ok: polarion-velocity-ssti — SecureUberspector inline literal
    public VelocityEngine buildAnotherEngine() {
        Properties props = new Properties();
        // Inline literal mention is what the rule's pattern-not-regex looks for.
        // Class-scope constants are a known FP pattern — see rule comment.
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        return new VelocityEngine(props);
    }
}
