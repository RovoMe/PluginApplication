package at.rovo.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks a field as target of an injection. Note that a class 
 * needs a {@link Component} annotation to be a valid target for 
 * injection.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Inject
{    
    boolean required() default true;
    Class<?> specificType() default DEFAULT.class;
            
    final class DEFAULT {}
}