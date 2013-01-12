package at.rovo.core;

/**
 * <p>If a class needs information on loading, unloading or
 * certain exceptions the {@link IPlugin} instance has thrown
 * it has to implement this interface to be notified when those
 * events occur.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IPluginListener 
{
	/**
	 * <p>Is triggered after a {@link IPlugin} object was 
	 * successfully loaded by the {@link PluginManager}.</p>
	 * 
	 * @param pluginName The name of the {@link IPlugin} which 
	 *                   got loaded
	 */
	public void pluginLoaded(String pluginName);
	
	/**
	 * <p>Is triggered after a {@link IPlugin} object was
	 * successfully removed by the {@link PluginManager}.</p>
	 * 
	 * @param pluginName The name of the {@link IPlugin} which
	 *                   was removed from the system.
	 */
	public void pluginRemoved(String pluginName);
	
	/**
	 * <p>Is triggered on catching any exceptions while loading 
	 * the {@link IPlugin} instance.</p>
	 * 
	 * @param pluginName The name of the {@link IPlugin} which
	 *                   produced the exception
	 * @param e The exception thrown by the {@link PluginManager}
	 *          while loading the plug-in
	 */
	public void exception(String pluginName, Exception e);
}
