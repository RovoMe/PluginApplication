package at.rovo.core.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A delegation class-loader for a set of {@link StrategyClassLoader}s. This
 * class-loader will act as a common layer for classes that need to be shared
 * among plug-ins. Therefore the plug-in has to contain an <code>export</code>
 * entry in the jars <em>MANIFEST.MF</em> file.
 * </p>
 * <p>
 * Classes marked as <em>export</em> will get loaded before any other classes of 
 * the plug-in. Every class marked as <em>export</em> will be added to a new 
 * composite {@link StrategyClassLoader} of this class loader via 
 * {@link #createLoaderForName(String, Set)}.
 * </p>
 * <p><em>Note:</em> The composite class-loaders managed by this delegation 
 * instance will be cached internally via WeakReferences. If there is no strong
 * reference pointing to the returned class-loader or to any resource loaded by 
 * that class-loader it might get eligible for garbage collection.</p>
 * 
 * @author Roman Vottner
 */
public final class DelegationClassLoader extends ClassLoader
{
	/** The logger of this class **/
	private static Logger LOGGER = 
			Logger.getLogger(DelegationClassLoader.class.getName());
	
	/** Caches the created composite class loaders, this class loader will act
	 * as parent for, with the name of the class that was marked as 
	 * <code>export</code>**/
	private Map<String, WeakReference<StrategyClassLoader>> commonLoaders = 
			new HashMap<>();
	
	/**
	 * <p>
	 * Creates a new instance of the class loader which adds the class-loader
	 * returned by {@ #getSystemClassLoader()} as parent of this instance.
	 * </p>
	 */
	public DelegationClassLoader()
	{
		super();
	}
	
	/**
	 * <p>
	 * Creates a new instance of the class loader which adds the provided 
	 * class-loader as parent of this instance.
	 * </p>
	 * 
	 * @param parent The parent loader of this instance
	 */
	public DelegationClassLoader(ClassLoader parent)
	{
		super(parent);
	}
	
	/**
	 * <p>
	 * On invoking this method a new composite {@link StrategyClassLoader} with 
	 * the given <em>strategy</em> will be created for the given <em>name</em>.
	 * </p>
	 * <p>
	 * <code>name</code> should therefore be the name of a class that is marked
	 * as exported within the plug-ins manifest file.
	 * </p>
	 * <p>
	 * Note that the created class-loader will be cached internally using 
	 * WeakReferences. This means if the reference to the returned object is 
	 * lost either through setting the object to null or as it was defined 
	 * within a code-block and no reference to a loaded object exist and the end
	 * of the block is reached, the created class-loader gets eligible for 
	 * garbage collection.
	 * </p>
	 * 
	 * @param name The name of the exported class a composite class loader 
	 *             should be created for calls will be delegated to
	 * @param strategies The strategy for the class loader to be set
	 * @return A strong reference to the class loader created
	 */
	public void addLoaderForName(String name, StrategyClassLoader loader)
	{
		loader.setName("Composite classloader for: "+name);
		WeakReference<StrategyClassLoader> ref = new WeakReference<>(loader);
		this.commonLoaders.put(name, ref);
	}
	
	/**
	 * <p>
	 * Removes the reference to a class-loader this instance delegates calls to.
	 * </p>
	 * <p>
	 * This method should only be called if a plug-in is requested to get 
	 * unloaded. Note further that this method does not guarantee that the 
	 * class-loader and all its resources it loaded get garbage collected.
	 * </p>
	 * 
	 * @param name
	 */
	public void unload(String name)
	{
		if (this.commonLoaders.containsKey(name))
			this.commonLoaders.remove(name);
	}
	
	@Override
	public Class<?> loadClass(String name)
	{
		LOGGER.log(Level.INFO, "Delegagion Loader - Request to load class {0}",
				new Object[] { name });
		try
		{
			Class<?> foundClass = null;
			List<String> removeLoader = new ArrayList<>();
			for (String loaderName : this.commonLoaders.keySet())
			{
				WeakReference<StrategyClassLoader> ref = 
						this.commonLoaders.get(loaderName);
				StrategyClassLoader loader = ref.get();
				if (loader != null && foundClass == null)
				{
					LOGGER.log(
							Level.INFO,
							"Delegagion Loader - Request to load class {0} - delegating to composit classloaders {1}",
							new Object[] { name, loader.getName() });
					Class<?> _foundClass = loader.hasLoadedClass(name);
					if (_foundClass != null)
					{
						LOGGER.log(Level.INFO, "Delegation: '{0}' - already loaded class '{1}'", 
								new Object[] { loader.getName(), name });
						foundClass = _foundClass;
					}
				}

				if (loader == null)
					removeLoader.add(loaderName);
			}

			for (String loaderName : removeLoader)
				this.commonLoaders.remove(loaderName);
			if (foundClass != null)
				return foundClass;

			LOGGER.log(Level.INFO, "Asking parent to load class {0}. ",
					new Object[] { name });

			try
			{
				if (this.getParent() != null)
					foundClass = this.getParent().loadClass(name);
				else
					foundClass = super.loadClass(name);
			}
			catch (ClassNotFoundException cnfEx)
			{
				LOGGER.log(
						Level.INFO,
						"Parent classloader didn't know how to load class {0}. Starting delegation.",
						new Object[] { name });
			}
			
			if (foundClass == null)
			{
				LOGGER.log(
						Level.INFO,
						"Delegation: Parent didn't find class '{0}' either! Invoking find",
						new Object[] { name });
				foundClass = this.findClass(name);
			}
				
			return foundClass;
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.log(Level.SEVERE, "Could not find class: {0}",
					new Object[] { name });
			// throw new PluginException("StrategyClassLoader.loadClass("
			// + name + "): " + e.getLocalizedMessage());
		}
		return null;
	}
	
	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException
	{		
		LOGGER.log(Level.INFO, "Delegagion Loader - Request to find class {0}", 
				new Object[] { className });
		
		// propagate the call to the registered strategies
		List<String> removeLoader = new ArrayList<>();
		Class<?> foundClass = null;
		boolean foundRequestor = false;
		for (String loaderName : this.commonLoaders.keySet())
		{
			WeakReference<StrategyClassLoader> ref = 
					this.commonLoaders.get(loaderName);
			StrategyClassLoader loader = ref.get();
			if (loader != null && foundClass == null)
			{
				try
				{
					Class<?> loadedClass = loader.hasLoadedClass(className);
					if (loadedClass != null)
					{
						LOGGER.log(Level.INFO, "Delegation: '{0}' - already loaded class {1}", 
								new Object[] { loader.getName(), className });
						foundClass = loadedClass;
						foundRequestor = true;
					}
					else
						LOGGER.log(Level.INFO, "Delegation: '{0}' - Didn't load class '{1}'", 
								new Object[] { loader.getName(), className });
					
//					if (loader.getRequestedClassToLoad() != null 
//							&& loader.getRequestedClassToLoad().equals(className))
//					{
//						LOGGER.log(Level.INFO, "Delegation: '{0}' - Requested to find class {1}", 
//								new Object[] { loader.getName(), className });
//						Class<?> _foundClass = loader.findClass(className);
//						foundRequestor = true;
//						
//						if (_foundClass != null)
//						{
//							foundClass = _foundClass;
//							
//							LOGGER.log(Level.FINE, 
//									"found bytes for class '{0}' - defining class", 
//									new Object[] {className});
//						}
//					}
//					else
//						LOGGER.log(Level.INFO, "Delegation: '{0}' - Didn't request to find class '{1}'", 
//								new Object[] { loader.getName(), className });
				}
//				catch(ClassNotFoundException cnfEx)
				catch(Exception cnfEx)
				{
					LOGGER.log(Level.SEVERE, 
							"Could not find bytes for class {0}", 
							new Object[] {className});
				}
			}
			
			if (loader == null)
				removeLoader.add(loaderName);
		}
		
		for (String loader : removeLoader)
			this.commonLoaders.remove(loader);
		
		if (!foundRequestor)
			LOGGER.log(Level.INFO, "Delegation: Child obviously request to find class '{0}'", 
					new Object[] { className });
		
		return foundClass;
	}

	@Override
	public URL getResource(String name)
	{
		// propagate the task to the strategies
		URL resource = null;
		List<String> removeLoader = new ArrayList<>();
		for (String loaderName : this.commonLoaders.keySet())
		{
			WeakReference<StrategyClassLoader> ref = 
					this.commonLoaders.get(loaderName);
			StrategyClassLoader loader = ref.get();
			
			// if we already found a resource don't look any further - just 
			// keep iterating through the loaders and check if one got unloaded
			if (loader != null && resource == null)
			{
				URL _resource = loader.getResource(name);
				if (_resource != null)
					resource = _resource;
			}
			
			if (loader == null)
				this.commonLoaders.remove(loaderName);
				
		}
		// now cleanup the cache if any loaders got unloaded
		if (!commonLoaders.isEmpty())
		{
			for (String loaderName : removeLoader)
				this.commonLoaders.remove(loaderName);
		}
		return resource;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		// propagate the task to the strategies
		Enumeration<URL> enumerationEnum = null;
		List<String> removeLoader = new ArrayList<>();
		for (String loaderName : this.commonLoaders.keySet())
		{
			WeakReference<StrategyClassLoader> ref = 
					this.commonLoaders.get(loaderName);
			StrategyClassLoader loader = ref.get();
			
			// if we already found a resource don't look any further - just 
			// keep iterating through the loaders and check if one got unloaded
			if (loader != null && enumerationEnum == null)
			{
				Enumeration<URL> _enumerationEnum = loader.getResources(name);
				if (_enumerationEnum != null)
					enumerationEnum = _enumerationEnum;
			}
			
			if (loader == null)
				this.commonLoaders.remove(loaderName);
				
		}
		// now cleanup the cache if any loaders got unloaded
		if (!commonLoaders.isEmpty())
		{
			for (String loaderName : removeLoader)
				this.commonLoaders.remove(loaderName);
		}
		return enumerationEnum;
	}

	@Override
	public InputStream getResourceAsStream(String source)
	{
		// propagate the task to the strategies
		InputStream stream = null;
		List<String> removeLoader = new ArrayList<>();
		for (String name : this.commonLoaders.keySet())
		{
			WeakReference<StrategyClassLoader> ref = 
					this.commonLoaders.get(name);
			StrategyClassLoader loader = ref.get();
			
			// if we already found a resource don't look any further - just 
			// keep iterating through the loaders and check if one got unloaded
			if (loader != null && stream == null)
			{
				InputStream _stream = getResourceAsStream(source);
				if (_stream != null)
					stream = _stream;
			}
			
			if (loader == null)
				this.commonLoaders.remove(name);
		}
		// now cleanup the cache if any loaders got unloaded
		if (!commonLoaders.isEmpty())
		{
			for (String name : removeLoader)
				this.commonLoaders.remove(name);
		}
		return null;
	}
	
	/**
	 * <p>
	 * Returns true if a composite classloader has loaded a class with the
	 * provided <em>className</em>, false if no classloader has loaded that
	 * class.
	 * </p>
	 * 
	 * @param className
	 *            The name of the class which should be checked if it is already
	 *            available
	 * @return true if the specified class was loaded by one of the composite
	 *         classloaders, false otherwhise
	 */
	public boolean containsClass(String className)
	{
		boolean hasLoadedClass = false;
		List<String> removeLoader = new ArrayList<>();
		for (String name : this.commonLoaders.keySet())
		{
			WeakReference<StrategyClassLoader> ref = 
					this.commonLoaders.get(name);
			StrategyClassLoader loader = ref.get();
			
			// if we already found a classloader that loaded the requested class
			// just keep iterating through the loaders and check if one got 
			// unloaded
			if (loader != null && !hasLoadedClass)
			{
				if (null != loader.hasLoadedClass(className))
				{
					hasLoadedClass = true;
				}
			}
			
			if (loader == null)
				this.commonLoaders.remove(name);
		}
		// now cleanup the cache if any loaders got unloaded
		if (!commonLoaders.isEmpty())
		{
			for (String name : removeLoader)
				this.commonLoaders.remove(name);
		}
		return hasLoadedClass;
	}
}
