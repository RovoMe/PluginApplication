package at.rovo.core;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import at.rovo.core.classloader.IClassLoaderStrategy;
import at.rovo.core.classloader.InjectionLoaderStrategy;
import at.rovo.core.classloader.PluginLoaderStrategy;
import at.rovo.core.classloader.StrategyClassLoader;
import at.rovo.plugin.IPlugin;

public class InjectionPluginManager extends PluginManager
{
	private static InjectionPluginManager instance = null;
	private StrategyClassLoader<IPlugin> singletonClassLoader = null;
	private InjectionLoaderStrategy injectionStrategy = null;
	private PluginLoaderStrategy pluginStrategy = new PluginLoaderStrategy();
	
	private InjectionPluginManager()
	{
		super();
		
		this.pluginStrategy = new PluginLoaderStrategy();
		this.singletonClassLoader = new StrategyClassLoader<IPlugin>(this.getClass().getClassLoader());
		this.injectionStrategy = new InjectionLoaderStrategy(this.pluginStrategy);
		this.singletonClassLoader.addLoader(this.injectionStrategy);
	}
	
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
			
			List<String> foundFiles = ClassFinder.scanJarFileForClasses(jarFile);
			
			this.injectionStrategy.setJarFile(jarFile);
			
			Set<IClassLoaderStrategy> strategy = new LinkedHashSet<IClassLoaderStrategy>();
			PluginLoaderStrategy pluginStrategy = new PluginLoaderStrategy(fileURL);
			InjectionLoaderStrategy injectionStrategy = new InjectionLoaderStrategy(pluginStrategy);
			strategy.add(injectionStrategy);
			injectionStrategy.setJarFile(jarFile);
					
			StrategyClassLoader<IPlugin> pluginLoader = new StrategyClassLoader<IPlugin>(this.singletonClassLoader, strategy);
			
			// load all classes for this plug-in
			Class<IPlugin> plugin = null;
			for (String className : foundFiles)
			{
				Class<?> clazz = this.loadPlugin(className, pluginLoader, fileURL);

				if (clazz != null)
				{
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> iface : interfaces)
					{
						if (iface.getName().equals(IPlugin.class.getName()))
						{
							meta.setClassObj((Class<IPlugin>)clazz);
							meta.setClassLoader(pluginLoader);
							plugin = (Class<IPlugin>)clazz;
							break;
						}
					}
				}
			}
				
			if (plugin != null)
			{
				for (IPluginListener listener : this.listeners)
					listener.pluginLoaded(pluginName);
			}
		}
		catch (Exception e)
		{
			for (IPluginListener listener : this.listeners)
				listener.exception(e);
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
			InjectionLoaderStrategy injector = null;
			for (IClassLoaderStrategy strategy : loader.getLoaders())
			{
				if (strategy instanceof InjectionLoaderStrategy)
				{
					injector = (InjectionLoaderStrategy)strategy;
					break;
				}
			}
			if (injector != null)
			{
				if (injector.isSingleton(pluginName))
				{
					// add the jar file to the class path of the plug-in class loader
					// to enable the loading of singleton classes
					URL cpBefore = this.pluginStrategy.getClassPath();
					this.pluginStrategy.setClassPath(fileURL);
					// load the class
					result = this.singletonClassLoader.loadClass(pluginName);
					// and remove the added jar file to prevent following classes
					// of being loaded by the singleton class loader
					this.pluginStrategy.setClassPath(cpBefore);
//					System.out.println("[PluginManager.loadPlugin] Used Singleton-ClassLoader for "+result+" with classloader "+result.getClassLoader());
				}
				else
				{
					loader.addLoader(injector);
					result = loader.loadClass(pluginName);
//					System.out.println("[PluginManager.loadPlugin] Used plugin-specific-ClassLoader for "+result+" with classloader "+result.getClassLoader());
				}
			}
			else
			{
				result = loader.loadClass(pluginName);
//				System.out.println("[PluginManager.loadPlugin] Used a plug-in ClassLoader for "+result+" with classloader "+result.getClassLoader());
			}
			
			return result;
		}
		catch (Exception e)
		{
			System.err.println(e.getLocalizedMessage());
		}
		return null;
	}
}
