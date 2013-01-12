package at.rovo.plugin;

/**
 * <p>A plug-in is a java class which gets loaded at runtime of the application
 * which uses it to extend its basic features. To enable the plug-in mechanism
 * a dynamically added class file needs to implement this interface to be 
 * recognized as a plug-in.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IPlugin 
{
	/**
	 * <p>Invokes execution of the plug-ins main class</p>
	 */
	public void execute();
}
