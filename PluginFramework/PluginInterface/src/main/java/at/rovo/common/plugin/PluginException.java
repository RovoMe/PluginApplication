package at.rovo.common.plugin;

public class PluginException extends RuntimeException 
{
	/** The unique Id of this serializable class **/
	private static final long serialVersionUID = -8282485938971656221L;
	
    /**
     * <p>Creates a new instance of <code>PluginException</code> without 
     * detail message.</p>
     */
    public PluginException() 
    {
    }

    /**
     * <p>Constructs an instance of <code>PluginException</code> with the 
     * specified detail message.</p>
     * 
     * @param msg The detail message
     */
    public PluginException(String msg) 
    {
        super(msg);
    }

    /**
     * <p>Constructs an instance of <code>PluginException</code> wrapping 
     * the specified throwable.</p>
     * 
     * @param t The throwable to wrap
     */
    public PluginException(Throwable t) 
    {
        super(t);
    }
}
