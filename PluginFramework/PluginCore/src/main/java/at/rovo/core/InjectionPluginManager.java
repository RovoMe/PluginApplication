package at.rovo.core;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.core.classloader.IClassLoaderStrategy;
import at.rovo.core.classloader.InjectionLoaderStrategyDecorator;
import at.rovo.core.classloader.PluginLoaderStrategy;
import at.rovo.core.classloader.StrategyClassLoader;
import at.rovo.core.injection.InjectionControllerImpl;
import at.rovo.core.util.ClassFinder;
import at.rovo.plugin.IPlugin;
import at.rovo.plugin.PluginException;

/**
 * <p>
 * <code>InjectionPluginManager</code> is a {@link PluginManager} that enables
 * injection of {@link at.rovo.core.injection.annotations.Inject} annotated
 * fields into classes annotated with
 * {@link at.rovo.core.injection.annotations.Component}. The injection target
 * has to be a {@link at.rovo.core.injection.annotations.Component} annotated
 * class too.
 * </p>
 * <p>
 * It is further able to handle singleton marked components as singletons which
 * ensures that a singleton component is only instantiated once while every
 * other component is instantiated on every injection.
 * </p>
 * <p>
 * Note that every class annotated with
 * {@link at.rovo.core.injection.annotations.Component} needs a private field of
 * type long which is annotated with
 * {@link at.rovo.core.injection.annotations.ComponentId}
 * </p>
 * <p>
 * To provide the injection mechanism {@link InjectionLoaderStrategyDecorator}
 * compiles a call to a {@link at.rovo.core.injection.IInjectionController} into
 * the component, which handles the injection of
 * {@link at.rovo.core.injection.annotations.Inject} annotated fields.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 * @see SimplePluginManager
 */
public class InjectionPluginManager extends PluginManager
{
	/** The logger of this class **/
	private static Logger logger = 
			Logger.getLogger(InjectionPluginManager.class.getName());
	/** The reference to the one and only instance of the InjectionPluginManager **/
	private static InjectionPluginManager INSTANCE = null;
	
	/**
	 * <p>Creates a new instance of this class and initializes required fields</p>
	 */
	private InjectionPluginManager()
	{
		super();
	}
	
	/**
	 * <p>
	 * Creates a new instance of the InjectionPluginManager if non was created
	 * before or returns the current instance for this plug-in manager.
	 * </p>
	 * 
	 * @return The instance of the console
	 */
	public static InjectionPluginManager getInstance()
	{
		if (INSTANCE == null)
		{
			synchronized (InjectionPluginManager.class)
			{
				if (INSTANCE == null)
				{
					INSTANCE = new InjectionPluginManager();
				}
			}
		}
		return INSTANCE;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void reloadPlugin(String pluginName)
	{
		PluginMeta meta = this.pluginData.get(pluginName);
		try
		{
			URL fileURL = meta.getJarFileURL();
			File jarFile = new File(fileURL.toURI());
			
			// get all classes provided by the jar file
			List<String> foundFiles = ClassFinder.scanJarFileForClasses(jarFile);
					
			// set the strategy for loading plug-ins
			Set<IClassLoaderStrategy> strategy = new LinkedHashSet<>();
			PluginLoaderStrategy pluginStrategy = 
					new PluginLoaderStrategy(fileURL);
			
			// decorate the strategy to insert new code into the class bytes
			InjectionLoaderStrategyDecorator injectionStrategy = 
					new InjectionLoaderStrategyDecorator(pluginStrategy);
			strategy.add(injectionStrategy);
			injectionStrategy.setJarFile(jarFile);
			
			// loads classes that are marked as to export with a new, separate 
			// classloader and add the classes to the common classloader 
			this.loadExportedClasses(meta, strategy);
					
			// create a new class loader for this plug-in which holds all 
			// non-exported classes
			StrategyClassLoader<IPlugin> pluginLoader = 
					new StrategyClassLoader<>(this.getClass().getClassLoader(), strategy);
			
			// load all classes for this plug-in with our new class loader
			Class<IPlugin> plugin = null;
			for (String className : foundFiles)
			{
				// load the class object for the respective class
				Class<?> clazz = this.loadPlugin(meta, className, pluginLoader);
				
				if (clazz != null)
				{
					// check if we found a IPlugin implementation
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> iface : interfaces)
					{
						if (iface.getName().equals(IPlugin.class.getName()))
						{
							plugin = (Class<IPlugin>)clazz;
							meta.setClassObj(plugin);
							meta.setClassLoader(pluginLoader);
							break;
						}
					}
				}
			}
				
			if (plugin != null)
			{
				// notify listeners of the successful load of the plug-in
				for (IPluginListener listener : this.listeners)
					listener.pluginLoaded(pluginName);
			}
		}
		catch (Exception e)
		{
			for (IPluginListener listener : this.listeners)
				listener.exception(pluginName, e);
		}
	}
	
	/**
	 * <p>
	 * Loads a plug-in whose .jar or .zip-file got already loaded into the
	 * systems cache.
	 * </p>
	 * <p>
	 * Note that classes that are marked as to export within a jar should
	 * already have been loaded before and a reference within the meta data
	 * object therefore be available.
	 * </p>
	 * 
	 * @param meta
	 *            The plugin's meta definition which is used to extract the
	 *            exported classes which have already been loaded
	 * @param className
	 *            Full name of the class to load.
	 * @param loader
	 *            A reference to the plugin's classloader
	 */
	protected Class<?> loadPlugin(PluginMeta meta, String className, 
			StrategyClassLoader<IPlugin> loader)
	{
		logger.log(Level.FINE, "Trying to load {0}", 
				new Object[] { className });
		try
		{
			Class<?> result = null;
			// TODO: singleton instantiation is done by InjectionControllerImpl - here we should check if the class is marked as export or not
			// The delegation mechanism should automatically detect non-exported dependency required by exported classes, which are loaded with
			// the commonClassLoader, even if the class to load is by a child loader
			logger.log(Level.FINE, "Plugin Meta used for class {0}: {1}", 
					new Object[] { className, meta });
			
			// check if the class to load is marked as to export or if it is a 
			// non-exported class of the plugin
			if (meta != null && meta.isExported(className))
			{
				// exported classes got loaded already within 
				// reloadPlugin(String) and are stored within the meta data
				// of the plug-in so load the class from there
				Class<?> exportedClass = meta.getExportedClass(className);
				if (exportedClass == null)
				{
					logger.log(Level.SEVERE, "Class object for exported class {0} not found!", 
							new Object[] { className });
				}
				else
					logger.log(Level.INFO, "Used common ClassLoader for {0} with classloader {1}", 
						new Object[] { exportedClass, exportedClass.getClassLoader()});
			}
			else
			{
				result = loader.loadClass(className);
				logger.log(Level.INFO, "Used plugin-specific-ClassLoader for {0} with classloader {1}", 
						new Object[] {result, result.getClassLoader()});
			}
			
			return result;
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Caught exception during loading of plugin: {0} - Reason: {1}", 
					new Object[] {className, e.getLocalizedMessage()});
			throw new PluginException(e.getLocalizedMessage());
		}
	}
	
	@Override
	public void close()
	{
		InjectionControllerImpl.INSTANCE.close();
	}
}
