package at.rovo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a class as component. A component needs a
 * unique identifier which has to be annotated with
 * {@link ComponentId}
 * @author Roman Vottner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Component
{    
    public ScopeType scope() default ScopeType.PROTOTYPE;
}
