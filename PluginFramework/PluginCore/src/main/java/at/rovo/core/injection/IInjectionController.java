package at.rovo.core.injection;

import at.rovo.common.annotations.Component;
import at.rovo.common.annotations.Inject;
import at.rovo.common.plugin.InjectionException;

/**
 * The injection controller interface.
 *
 * @author Roman Vottner
 */
public interface IInjectionController
{
    /**
     * Injects all {@link Inject} annotated fields. Only objects of classes that are {@link Component} annotated shall
     * be accepted.
     *
     * @param obj
     *         the object to initialize
     *
     * @return The injected object
     *
     * @throws InjectionException
     *         if it's no component or any injection fails
     */
    Object initialize(Object obj) throws InjectionException;

    /**
     * Gets the fully initialized instance of a singleton component. Multiple calls of this method always have to return
     * the same instance.
     * <p/>
     * Only classes that are {@link Component} annotated shall be accepted.
     *
     * @param <T>
     *         the class type.
     * @param clazz
     *         the class of the component.
     *
     * @return the initialized singleton object.
     *
     * @throws InjectionException
     *         if it's no component or any injection fails.
     */
    <T> T getSingletonInstance(Class<T> clazz) throws InjectionException;
}
