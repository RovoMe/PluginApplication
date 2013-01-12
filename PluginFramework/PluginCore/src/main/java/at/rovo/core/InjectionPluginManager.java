package at.rovo.core;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import at.rovo.core.classloader.IClassLoaderStrategy;
import at.rovo.core.classloader.InjectionLoaderStrategyDecorator;
import at.rovo.core.classloader.PluginLoaderStrategy;
import at.rovo.core.classloader.StrategyClassLoader;
import at.rovo.plugin.IPlugin;
import at.rovo.plugin.PluginException;

/**
 * <p><code>InjectionPluginManager</code> is a {@link PluginManager} that enables
 * injection of {@link at.rovo.core.injection.annotations.Inject} annotated fields into classes 
 * annotated with {@link at.rovo.core.injection.annotations.Component}. The injection target 
 * has to be a {@link at.rovo.core.injection.annotations.Component} annotated class too.</p>
 * <p>It is further able to handle singleton marked components as singletons
 * which ensures that a singleton component is only instantiated once while
 * every other component is instantiated on every injection.</p>
 * <p>Note that every class annotated with {@link at.rovo.core.injection.annotations.Component} 
 * needs a private field of type long which is annotated with 
 * {@link at.rovo.core.injection.annotations.ComponentId}</p>
 * <p>To provide the injection mechanism {@link InjectionLoaderStrategyDecorator} compiles
 * a call to a {@link at.rovo.core.injection.IInjectionController} into the 
 * component, which handles the injection of {@link at.rovo.core.injection.annotations.Inject} 
 * annotated fields.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 * @see SimplePluginManager
 */
public class InjectionPluginManager extends PluginManager
{
	/** The reference to the one and only instance of the InjectionPluginManager **/
	private static InjectionPluginManager instance = null;
	/** The class loader which holds the singleton components **/
	private StrategyClassLoader<IPlugin> singletonClassLoader = null;
	/** The decorator for the plug-in loader **/
	private InjectionLoaderStrategyDecorator injectionStrategy = null;
	/** The strategy pattern for loading plug-ins **/
	private PluginLoaderStrategy pluginStrategy = new PluginLoaderStrategy();
	
	/**
	 * <p>Creates a new instance of this class and initializes required fields</p>
	 */
	private InjectionPluginManager()
	{
		super();
		
		// create the class loader which will hold the singleton class definitions and 
		// therefore be responsible for their creation 
		this.singletonClassLoader = new StrategyClassLoader<>(this.getClass().getClassLoader());
		// set the strategy to load plugins with the singleton class loader
		// and decorate the strategy with an injection mechanism
		this.pluginStrategy = new PluginLoaderStrategy();
		this.injectionStrategy = new InjectionLoaderStrategyDecorator(this.pluginStrategy);
		this.singletonClassLoader.addStrategy(this.injectionStrategy);
	}
	
	/**
	 * <p>Creates a new instance of the InjectionPluginManager if non was created 
	 * before or returns the current instance for this plug-in manager.</p>
	 * 
	 * @return The instance of the console
	 */
	public static PluginManager getInstance()
	{
		if (instance == null)
			instance = new InjectionPluginManager();
		return instance;
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
			PluginLoaderStrategy pluginStrategy = new PluginLoaderStrategy(fileURL);
			// decorate the strategy to insert new code into the class bytes
			InjectionLoaderStrategyDecorator injectionStrategy = new InjectionLoaderStrategyDecorator(pluginStrategy);
			strategy.add(injectionStrategy);
			injectionStrategy.setJarFile(jarFile);
					
			// create a new class loader for this plug-in
			StrategyClassLoader<IPlugin> pluginLoader = new StrategyClassLoader<>(this.singletonClassLoader, strategy);
			
			// load all classes for this plug-in with our new class loader
			Class<IPlugin> plugin = null;
			for (String className : foundFiles)
			{
				Class<?> clazz = this.loadPlugin(className, pluginLoader, fileURL);
				
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
	 * <p>Loads a plug-in whose .jar or .zip-file got already loaded
	 * into the systems cache.</p>
	 * @param pluginName Full name of the plug-in to load.
	 */
	protected Class<?> loadPlugin(String pluginName, StrategyClassLoader<IPlugin> loader, URL fileURL)
	{
		try
		{
			// Check if the class to load is a singleton
			// if so, use a single class-loader therefore
			// if not, use a class-loader which is a child of
			// the singleton class-loader so that a lookup
			// for the singleton of every class loaded by
			// a child class-loader will return this singleton
			Class<?> result = null;
			InjectionLoaderStrategyDecorator injector = null;
			for (IClassLoaderStrategy strategy : loader.getStrategies())
			{
				if (strategy instanceof InjectionLoaderStrategyDecorator)
				{
					injector = (InjectionLoaderStrategyDecorator)strategy;
					break;
				}
			}
			if (injector != null)
			{
				if (injector.isSingleton(pluginName))
				{
					// add the jar file to the strategy decorator so it is able to
					// inject the invocation code for the IInjectionController
					// into the class file's bytes
					this.injectionStrategy.setJarFile(new File(fileURL.toURI()));
					
					// add the jar file to the class path of the plug-in class loader
					// to enable the loading of singleton classes
					URL cpBefore = this.pluginStrategy.getClassPath();
					this.pluginStrategy.setClassPath(fileURL);
					// load the class
					result = this.singletonClassLoader.loadClass(pluginName);
					// and remove the added jar file to prevent following classes
					// from being loaded by the singleton class loader
					this.pluginStrategy.setClassPath(cpBefore);
					System.out.println("[PluginManager.loadPlugin] Used Singleton-ClassLoader for "+result+" with classloader "+result.getClassLoader());
				}
				else
				{
					loader.addStrategy(injector);
					result = loader.loadClass(pluginName);
					System.out.println("[PluginManager.loadPlugin] Used plugin-specific-ClassLoader for "+result+" with classloader "+result.getClassLoader());
				}
			}
			else
			{
				result = loader.loadClass(pluginName);
				System.out.println("[PluginManager.loadPlugin] Used a plug-in ClassLoader for "+result+" with classloader "+result.getClassLoader());
			}
			
			return result;
		}
		catch (Exception e)
		{
			throw new PluginException(e.getLocalizedMessage());
		}
	}
}