package ch.sbb.polarion.extension.example.velocity;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.introspection.SecureUberspector;
import java.util.Properties;

public class VelocitySstiFixed {

    // ok: polarion-velocity-ssti — hardened in the same method, return form
    public VelocityEngine buildEngine() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        return new VelocityEngine(props);
    }

    // ok: polarion-velocity-ssti — hardened in the same method, local-variable form
    public void useEngine() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        VelocityEngine engine = new VelocityEngine(props);
        engine.init();
    }
}

// ok: polarion-velocity-ssti — the class named rather than spelled as a literal
class VelocitySstiFixedClassLiteral {

    public VelocityEngine buildEngine() {
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        return new VelocityEngine(props);
    }
}

// ok: polarion-velocity-ssti — a field initializer cleared by hardening
// elsewhere in the same class, which is the only scope available to it
class VelocitySstiFixedFieldInitializer {

    private static final VelocityEngine ENGINE = new VelocityEngine(secureProperties());

    private static Properties secureProperties() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        return props;
    }
}

// ok: polarion-velocity-ssti — hardened in a static initializer
class VelocitySstiFixedStaticInitializer {

    private static final Properties PROPS = new Properties();

    static {
        PROPS.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
    }

    private static final VelocityEngine ENGINE = new VelocityEngine(PROPS);
}

// ok: polarion-velocity-ssti — hardened in a constructor
class VelocitySstiFixedConstructor {

    private final VelocityEngine engine;

    VelocitySstiFixedConstructor() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.SecureUberspector");
        this.engine = new VelocityEngine(props);
    }
}

// ok: polarion-velocity-ssti — the FQCN held in a class-scope constant, which
// semgrep propagates into the configuring call
class VelocitySstiFixedConstant {

    private static final String UBERSPECT =
        "org.apache.velocity.util.introspection.SecureUberspector";

    public VelocityEngine buildEngine() {
        Properties props = new Properties();
        props.setProperty("runtime.introspector.uberspect", UBERSPECT);
        return new VelocityEngine(props);
    }
}

// ok: polarion-velocity-ssti — the class named rather than spelled as a literal,
// from a constructor, where no method-scoped clause can apply
class VelocitySstiFixedConstructorClassLiteral {

    private final VelocityEngine engine;

    VelocitySstiFixedConstructorClassLiteral() {
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        this.engine = new VelocityEngine(props);
    }
}

// ok: polarion-velocity-ssti — the same spelling from a static initializer
class VelocitySstiFixedStaticInitializerClassLiteral {

    private static final Properties PROPS = new Properties();

    static {
        PROPS.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
    }

    private static final VelocityEngine ENGINE = new VelocityEngine(PROPS);
}

// ok: polarion-velocity-ssti — an enum singleton, which `class $CLASS { ... }`
// matches, so the class-scoped clauses reach it
enum VelocitySstiFixedEnum {

    INSTANCE;

    private final VelocityEngine engine;

    VelocitySstiFixedEnum() {
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        this.engine = new VelocityEngine(props);
    }

    VelocityEngine engine() {
        return engine;
    }
}
