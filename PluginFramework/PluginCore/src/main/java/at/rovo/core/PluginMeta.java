package at.rovo.core;

import java.net.MalformedURLException;
import java.net.URL;
import at.rovo.plugin.IPlugin;

/**
 * <p>
 * On loading a {@link IPlugin} implementation the responsible
 * {@link PluginManager} creates some meta data for the plug-in which will be
 * stored in a new instance of this class.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class PluginMeta
{
	/** The name of the plug-in **/
	private String name = null;
	/** The class which implements the IPlugin interface **/
	private String declaredClass = null;
	/** The class loader which loaded the plug-in **/
	private ClassLoader classLoader = null;
	/** The jar file containing the plug-in **/
	private URL jarFile = null;
	/** The IPlugin implementing class **/
	private Class<IPlugin> pluginClass = null;
	/** The loaded and initialized plug-in **/
	private IPlugin plugin = null;

	/**
	 * <p>
	 * Sets the name of the plug-in to the defined value.
	 * </p>
	 * 
	 * @param name
	 *            The new name of the plug-in this meta data points to
	 */
	public void setPluginName(String name)
	{
		this.name = name;
	}

	/**
	 * <p>
	 * The name of the plug-in this instance is pointing to
	 * </p>
	 * 
	 * @return The name of the plug-in
	 */
	public String getPluginName()
	{
		return this.name;
	}

	/**
	 * <p>
	 * Sets the name of the class which implements the {@link IPlugin}
	 * interface.
	 * </p>
	 * 
	 * @param className
	 *            The name of the implementing class
	 */
	public void setDeclaredClassName(String className)
	{
		this.declaredClass = className;
	}

	/**
	 * <p>
	 * Returns the name of the {@link IPlugin} implementing class.
	 * </p>
	 * 
	 * @return The name of the {@link IPlugin} implementing class
	 */
	public String getDeclaredClassName()
	{
		return this.declaredClass;
	}

	/**
	 * <p>
	 * Sets the {@link ClassLoader} for the plug-in this instance is pointing
	 * to.
	 * </p>
	 * 
	 * @param cl
	 *            The class loader of the plug-in
	 */
	public void setClassLoader(ClassLoader cl)
	{
		this.classLoader = cl;
	}

	/**
	 * <p>
	 * Returns the {@link ClassLoader} of the plug-in this instance is pointing
	 * to.
	 * </p>
	 * 
	 * @return The class loader of the plug-in
	 */
	public ClassLoader getClassLoader()
	{
		return this.classLoader;
	}

	/**
	 * <p>
	 * Sets the {@link URL} of the jar file that contains the plug-in.
	 * </p>
	 * 
	 * @param jarFile
	 *            The {@link URL} of the jar-file containing the plug-in
	 */
	public void setJarFileURL(URL jarFile)
	{
		this.jarFile = jarFile;
	}

	/**
	 * <p>
	 * Sets the path to the jar file that contains the plug-in.
	 * </p>
	 * 
	 * @param jarFile
	 *            The path of the jar-file containing the plug-in
	 */
	public void setJarFileName(String jarFile)
	{
		try
		{
			this.jarFile = new URL(jarFile);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Returns the {@link URL} of the jar containing the plug-in.
	 * </p>
	 * 
	 * @return The {@link URL} of the jar containing the plug-in
	 */
	public URL getJarFileURL()
	{
		return this.jarFile;
	}

	/**
	 * <p>
	 * Returns the name of the jar file containing the plug-in.
	 * </p>
	 * 
	 * @return The name of the jar file containing the plug-in
	 */
	public String getJarFileName()
	{
		String fileName = this.jarFile.toString();
		if (fileName.startsWith("file:"))
			fileName = fileName.substring("file:".length());
		return fileName;
	}

	/**
	 * <p>
	 * Sets the loaded class object of the class that implements the
	 * {@link IPlugin} interface.
	 * </p>
	 * 
	 * @param plugin
	 *            The class object of the implementing plug-in
	 */
	public void setClassObj(Class<IPlugin> plugin)
	{
		this.pluginClass = plugin;
	}

	/**
	 * <p>
	 * Returns the class object of the class that implements the {@link IPlugin}
	 * interface.
	 * </p>
	 * 
	 * @return The class object of the implementing plug-in
	 */
	public Class<IPlugin> getClassObj()
	{
		return this.pluginClass;
	}

	/**
	 * <p>
	 * Sets the loaded and initialized plugin instance. The current meta 
	 * instance will hold this plugin instance until an unload request is 
	 * received. This way garbage collection of any object held by the plugin
	 * will be prevented until the plugin is actually unloaded.
	 * </p>
	 * 
	 * @param plugin The initialized plug-ins main class 
	 */
	public void setPlugin(IPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	/**
	 * <p>
	 * Returns the plugins main object.
	 * </p>
	 * 
	 * @return The initialized plugin main class
	 */
	public IPlugin getPlugin()
	{
		return this.plugin;
	}
}