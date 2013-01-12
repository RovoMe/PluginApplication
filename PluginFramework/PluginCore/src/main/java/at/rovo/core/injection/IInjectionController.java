package at.rovo.core.injection;

import at.rovo.annotations.Component;
import at.rovo.annotations.Inject;
import at.rovo.plugin.InjectionException;

/**
 * The injection controller interface.
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IInjectionController 
{
    /**
     * <p>Injects all {@link Inject} annotated fields. Only objects of classes 
     * that are {@link Component} annotated shall be accepted.</p>
     * 
     * @param obj the object to initialize
     * @return The injected object
     * @throws InjectionException if it's no component or any injection fails
     */
    Object initialize(Object obj) throws InjectionException;

    /**
     * <p>Gets the fully initialized instance of a singleton component. Multiple 
     * calls of this method always have to return the same instance.</p>
     * <p>Only classes that are {@link Component} annotated shall be accepted.</p>
     * 
     * @param <T> the class type.
     * @param clazz the class of the component.
     * @return the initialized singleton object.
     * @throws InjectionException if it's no component or any injection fails.
     */
    <T> T getSingletonInstance(Class<T> clazz) throws InjectionException;
}
