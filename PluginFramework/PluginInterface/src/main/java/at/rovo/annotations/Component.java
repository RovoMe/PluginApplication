package at.rovo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Marks a class as component. A component needs a
 * unique identifier which has to be annotated with
 * {@link ComponentId}.</p>
 * <p>The {@link ScopeType} defines the number of instances
 * that may be created for an annotated component.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Component
{    
    public ScopeType scope() default ScopeType.PROTOTYPE;
}
