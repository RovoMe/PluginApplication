package at.rovo.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	/** Will hold a strong reference to the exported classes **/
	private Map<String, Class<?>> exportedClasses = new HashMap<>();
	/** Will hold a strong reference to the classes required by this plugin **/
	private Map<String, Class<?>> requiredClasses = new HashMap<>();

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
	 * @param plugin
	 *            The initialized plug-ins main class
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

	/**
	 * <p>
	 * Adds only the name of the as exported annotated class to the plugin meta
	 * description.
	 * </p>
	 * 
	 * @param export
	 *            The set containing the classes which are marked as to export
	 */
	public void setExportedClassSet(List<String> export)
	{
		if (this.exportedClasses == null)
			this.exportedClasses = new HashMap<>();
			
		for (String name : export)
			this.exportedClasses.put(name, null);
	}

	/**
	 * <p>
	 * Adds a class which marked as to export to this plugin metadata.
	 * </p>
	 * 
	 * @param className
	 *            The name of the exported class
	 * @param clazz
	 *            The class representation of the required class
	 */
	public void addExpordedClass(String className, Class<?> clazz)
	{
		if (this.exportedClasses == null)
			this.exportedClasses = new HashMap<>();
		this.exportedClasses.put(className, clazz);
	}

	/**
	 * <p>
	 * Returns true if the provided class name is among the exported classes.
	 * </p>
	 * 
	 * @param classNameThe
	 *            class name to check if it is exported
	 * @return true if the provided classname equals an exported class
	 */
	public boolean isExported(String className)
	{
		if (this.exportedClasses != null)
			return this.exportedClasses.containsKey(className);
		return false;
	}

	/**
	 * <p>
	 * Returns the names of the exported classes by this plugin.
	 * </p>
	 * 
	 * @return The names of the exported classes by this plugin
	 */
	public Set<String> getExportedClasses()
	{
		if (this.exportedClasses != null)
			return this.exportedClasses.keySet();
		return Collections.emptySet();
	}
	
	/**
	 * <p>
	 * Removes all references to classes marked as to export.
	 * </p>
	 */
	public void removeExportedClasses()
	{
		if (this.exportedClasses != null)
		{
			Set<String> classNames = new HashSet<>(this.exportedClasses.keySet());
			for (String className : classNames)
			{
				this.exportedClasses.remove(className);
			}
		}
	}
	
	/**
	 * <p>
	 * Returns the class object of the provided class name which was defined as
	 * to export.
	 * </p>
	 * 
	 * @param className
	 *            The name of the exported class
	 * @return The class object of the exported class
	 */
	public Class<?> getExportedClass(String className)
	{
		if (this.exportedClasses != null)
			return this.exportedClasses.get(className);
		return null;
	}
	
	/**
	 * <p>
	 * Adds only the name of the as required annotated class to the plugin meta
	 * description.
	 * </p>
	 * 
	 * @param required
	 *            The set containing the classes which are marked as to export
	 */
	public void setRequiredClassSet(List<String> required)
	{
		if (this.requiredClasses == null)
			this.requiredClasses = new HashMap<>();
		for (String name : required)
			this.requiredClasses.put(name, null);
	}
	
	/**
	 * <p>
	 * Adds a class which is required by this plugin to this metadata object.
	 * </p>
	 * 
	 * @param className
	 *            The name of the by this plugin required class
	 * @param clazz
	 *            The class which is required by this plugin
	 */
	public void addRequiredClass(String className, Class<?> clazz)
	{
		if (this.requiredClasses == null)
			this.requiredClasses = new HashMap<>();
		this.requiredClasses.put(className, clazz);
	}

	/**
	 * <p>
	 * Returns true if this plugin requires the class with the given class name.
	 * </p>
	 * 
	 * @param className
	 *            The class name to check if it is required
	 * @return true if the provided class name is needed by this plugin
	 */
	public boolean isRequiredClasses(String className)
	{
		if (this.requiredClasses != null)
			return this.requiredClasses.containsKey(className);
		return false;
	}
	
	/**
	 * <p>
	 * Returns all as required marked classes for this plugin.
	 * </p>
	 * 
	 * @return The names of the required classes by this plugin
	 */
	public Set<String> getRequiredClasses()
	{
		if (this.requiredClasses != null)
			return this.requiredClasses.keySet();
		return Collections.emptySet();
	}
	
	/**
	 * <p>
	 * Returns the loaded class object for the provided class name.
	 * </p>
	 * 
	 * @param className
	 *            The name of the required class
	 * @return The class object of the required class
	 */
	public Class<?> getRequiredClass(String className) 
	{
		if (this.requiredClasses != null)
			return this.requiredClasses.get(className);
		return null;
	}
	
	/**
	 * <p>
	 * Removes all references to classes marked as required.
	 * </p>
	 */
	public void removeRequiredClasses()
	{
		if (this.requiredClasses != null)
		{
			Set<String> classNames = new HashSet<>(this.requiredClasses.keySet());
			for (String className : classNames)
			{
				this.requiredClasses.remove(className);
			}
		}
	}
}