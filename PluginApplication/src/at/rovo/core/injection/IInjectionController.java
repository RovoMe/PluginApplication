package at.rovo.core.injection;

/**
 * The injection controller interface.
 */
public interface IInjectionController 
{
    /**
     * Injects all Inject annotated fields/methods.
     * Only objects of classes that are Component annotated shall be accepted.
     * @param obj the object to initialize.
     * @return The injected object
     * @throws dst3.dynload.injection.InjectionException if it's no component or
     * any injection fails.
     */
    Object initialize(Object obj) throws InjectionException;

    /**
     * Gets the fully initialized instance of a singleton component.
     * Multiple calls of this method always have to return the same instance.
     * Only classes that are Component annotated shall be accepted.
     * @param <T> the class type.
     * @param clazz the class of the component.
     * @return the initialized singleton object.
     * @throws dst3.dynload.injection.InjectionException if it's no component or
     * any injection fails.
     */
    <T> T getSingletonInstance(Class<T> clazz) throws InjectionException;
}
