package at.rovo.core;

import java.io.File;

/**
 * <p><code>SimplePluginManager</code> manages the lifecycle of {@link IPlugin} 
 * implementing classes contained in a .jar archive.</p>
 * 
 * <p>It takes care of the loading of plug-ins in a specified directory, which 
 * can be set with the {@link #setPluginDirectory(String)}-Method.
 * Classes interested in notification of loading or unloading from or exception
 * occurred while loading a plug-in can register themselves with <code>
 * SimplePluginManager</code> via {@link #addPluginListener(IPluginListener)}.</p>
 * <p>This class provides several methods to actually load plug-ins:</p>
 * <li>{@link #loadPlugins()}: Loads all plug-ins located in the directory 
 *     defined by {@link #setPluginDirectory(String)}</li>
 * <li>{@link #loadAllPluginsFromDirectory(File)}: Loads all plug-ins located 
 *     in a directory which has to be provided as argument.</li>
 * <li>{@link #reloadPlugin(File)}: (Re)Loads a specific plug-in</li>
 * <p>Every plug-in gets loaded by a different {@link ClassLoader} to provide
 * a mechanism to unload an unneeded or reload an updated plug-in at runtime 
 * without having to tear down the whole system.</p>
 * <p>As with loading, <code>SimplePluginManager</code> provides two methods to
 * remove plug-ins from the system:</p>
 * <li>{@link #unloadAll()}: Unloads every loaded plug-in.</li>
 * <li>{@link #unload(String)}: Unloads a specific plug-in.</li>
 * <p>The unloading of loaded classes is not guaranteed as they get unloaded 
 * by the garbage collector. If any class does have a valid reference to a
 * class defined by the plug-in, unloading of the plug-in will fail.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 * @see InjectionPluginManager
 */
public class SimplePluginManager extends PluginManager
{
	/** The reference to the one and only instance of the SimplePluginManager **/
	private static SimplePluginManager instance = null;
	
	/**
	 * <p>Creates a new instance of this class.</p>
	 */
	private SimplePluginManager()
	{
		super();
	}
	
	/**
	 * <p>Creates a new instance of the SimplePluginManager if non was created 
	 * before or returns the current instance for this plug-in manager.</p>
	 * 
	 * @return The instance of the console
	 */
	public static PluginManager getInstance()
	{
		if (instance == null)
			instance = new SimplePluginManager();
		return instance;
	}
}