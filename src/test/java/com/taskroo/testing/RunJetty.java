package com.taskroo.testing;

import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtensionAnnotation(JettyExtension.class)
public @interface RunJetty {
    public abstract String context() default "";

    public abstract int port() default 8080;

    public abstract String host() default "http://localhost/";
}
