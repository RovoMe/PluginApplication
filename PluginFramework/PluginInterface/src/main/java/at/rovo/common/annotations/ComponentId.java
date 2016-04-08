package at.rovo.common.annotations;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks a field as unique identifier of a
 * {@link Component}. Note that every {@link Component}
 * may only have one unique identifier annotated
 * with this class.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface ComponentId
{
    
}
