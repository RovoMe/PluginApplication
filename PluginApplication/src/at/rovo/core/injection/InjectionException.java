package at.rovo.core.injection;

/**
 * Thrown whenever an injection problem occurs.
 */
public class InjectionException extends RuntimeException {

    private static final long serialVersionUID = 3221609361590670030L;

    /**
     * Creates a new instance of <code>InjectionException</code> without detail message.
     */
    public InjectionException() {
    }

    /**
     * Constructs an instance of <code>InjectionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InjectionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>InjectionException</code> wrapping the specified throwable.
     * @param t the throwable to wrap.
     */
    public InjectionException(Throwable t) {
        super(t);
    }
}
