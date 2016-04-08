package at.rovo.common.plugin;

/**
 * <p>Thrown whenever an injection problem occurs.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class InjectionException extends RuntimeException 
{
	/** The unique Id of this serializable class **/
    private static final long serialVersionUID = 3221609361590670030L;

    /**
     * <p>Creates a new instance of <code>InjectionException</code> without 
     * detail message.</p>
     */
    public InjectionException() 
    {
    }

    /**
     * <p>Constructs an instance of <code>InjectionException</code> with the 
     * specified detail message.</p>
     * 
     * @param msg The detail message
     */
    public InjectionException(String msg) 
    {
        super(msg);
    }

    /**
     * <p>Constructs an instance of <code>InjectionException</code> wrapping 
     * the specified throwable.</p>
     * 
     * @param t The throwable to wrap
     */
    public InjectionException(Throwable t) 
    {
        super(t);
    }
}
