package at.rovo.core.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.core.util.IteratorEnumeration;

/**
 * <p>
 * This {@link ClassLoader} implementation may handle different strategies to
 * load specific classes. This implementation is based on the strategy pattern
 * used by Ted Neward's implementation found in his book <em>'Java Server-Based 
 * Programming' (ISBN1-884777-71-6)</em>.
 * </p>
 * <p>
 * This implementation differs from Ted Neward's implementation by first looking
 * if the parental class loader has already loaded the required class, which
 * will then invoke {@link #defineClass(String, byte[], int, int)} and return a
 * valid <code>Class</code> object. If the parental class loaders are unable to
 * find the required class the current instance propagates the call to its
 * strategies. The first strategy that returns a byte[] array unequal to null
 * will load the class via the {@link #defineClass(String, byte[], int, int)}
 * method and return it to the caller.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public class StrategyClassLoader extends ClassLoader
{
	/** The logger of this class **/
	private static Logger LOGGER = 
			Logger.getLogger(StrategyClassLoader.class.getName());
	/** The registered strategies for this class loader **/
	private Set<IClassLoaderStrategy> strategies = null;
	/** The name of this class loader **/
	private String name = "Strategy class loader";

	/**
	 * <p>
	 * Creates a new instance of this class loader which is a child of the class
	 * loader that loaded this class loder's class file. This constructor will
	 * add an empty {@link Set} for storing strategies to use for class loading.
	 * </p>
	 */
	public StrategyClassLoader()
	{
		this(StrategyClassLoader.class.getClassLoader(),
				new HashSet<IClassLoaderStrategy>());
	}

	/**
	 * <p>
	 * Creates a new instance of this class loader which is a child of the class
	 * loader that loaded this class loder's class file. This constructor set
	 * the specified strategies as strategies to use for class loading.
	 * </p>
	 * 
	 * @param strategies
	 *            The strategies to use for class loading
	 */
	public StrategyClassLoader(Set<IClassLoaderStrategy> strategies)
	{
		this(StrategyClassLoader.class.getClassLoader(), strategies);
	}

	/**
	 * <p>
	 * Creates a new instance of this class loader which is a child of the
	 * defined <em>loader</em>. This constructor will add an empty {@link Set}
	 * for storing strategies to use for class loading.
	 * </p>
	 * </p>
	 * 
	 * @param parent
	 *            The {@link ClassLoader} to use as a parent class loader
	 */
	public StrategyClassLoader(ClassLoader parent)
	{
		this(parent, new HashSet<IClassLoaderStrategy>());
	}

	/**
	 * <p>
	 * Creates a new instance of this class loader which is a child of the
	 * defined <em>loader</em>. This constructor set the specified strategies as
	 * strategies to use for class loading.
	 * </p>
	 * 
	 * @param parent
	 *            The {@link ClassLoader} to use as a parent class loader
	 * @param strategies
	 *            The strategies to use for class loading
	 */
	public StrategyClassLoader(ClassLoader parent,
			Set<IClassLoaderStrategy> strategies)
	{
		super(parent);
		this.strategies = strategies;
	}

	/**
	 * <p>
	 * Adds a strategy to the set of used strategies for loading classes.
	 * </p>
	 * 
	 * @param strategy
	 *            The strategy to add to the set of strategies
	 */
	public void addStrategy(IClassLoaderStrategy strategy)
	{
		this.strategies.add(strategy);
	}

	/**
	 * <p>
	 * Returns all registered strategies for this class loader as a {@link Set}.
	 * </p>
	 * 
	 * @return A {@link Set} of all registered strategies for this class loader
	 */
	public Set<IClassLoaderStrategy> getStrategies()
	{
		return strategies;
	}

	/**
	 * <p>
	 * Sets the name of the classloader instance.
	 * </p>
	 * 
	 * @param name The new name of this instance
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * <p>
	 * Returns the name of this classloader instance.
	 * </p>
	 * 
	 * @return The name of this classloader instance
	 */
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public Class<?> loadClass(String name)
	{
		LOGGER.log(Level.INFO, "{0} - Request to load class {1}", 
				new Object[] { this.name, name });
		
		Class<?> classToLoad = null;
		try
		{
			ClassLoader parent = this.getParent();
			
			// check first if we haven't already loaded the class
			classToLoad = this.hasLoadedClass(name);
			if (classToLoad != null)
			{
				LOGGER.log(Level.INFO, "{0} - Load class '{1}' before", 
						new Object[] { this.name, name });
				return classToLoad;
			}
			
			LOGGER.log(Level.INFO, "{0} - Didn't load class '{1}' before - trying parent loader", 
					new Object[] { this.name, name });
			// not loaded by us, maybe the ancestor have loaded it?
			if (parent != null && parent instanceof DelegationClassLoader)
				classToLoad = this.getParent().loadClass(name);
			else
				classToLoad =  super.loadClass(name);
			
			// the parents didn't load the class either, so do it ourselves
			if (classToLoad == null)
			{
				LOGGER.log(Level.INFO, "{0} - Parent didn't load' {1}' either - finding it ourselves", 
						new Object[] { this.name, name });
				classToLoad = this.findClass(name);
			}
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.log(Level.SEVERE, "Could not find class: {0}",
					new Object[] { name });
//			throw new PluginException("StrategyClassLoader.loadClass(" + name
//					+ "): " + e.getLocalizedMessage());
		}
		return classToLoad;
	}

	@Override
	protected Class<?> findClass(String className)
			throws ClassNotFoundException
	{
		LOGGER.log(Level.INFO, "{0} - Request to find class {1}", 
				new Object[] { this.name, className });
		
		// do not define already defined classes again
		Class<?> clazz = this.findLoadedClass(className);
		if (clazz != null)
		{
			LOGGER.log(Level.INFO, "{0} has loaded {1} before", 
					new Object[] { this.name, className });
			return clazz;
		}
		else
		{
			LOGGER.log(Level.INFO, "{0} has not loaded {1} before", 
					new Object[] { this.name, className });
		}
		
		try
		{
			byte[] classBytes = this.findClassBytes(className);
			if (classBytes != null)
			{
				LOGGER.log(Level.INFO,
						"found bytes for class {0} - defining class in {1}",
						new Object[] { className, this.getName() });
				// at least one strategy was able to find bytes for this class
				// so create the class based on the found bytes
				return defineClass(className, classBytes, 0, classBytes.length);
			}
		}
		catch (IOException ioEx)
		{
			throw new ClassNotFoundException("Error while loading class "
					+ className + "! The following error occurred: "
					+ ioEx.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * <p>
	 * Finds the bytes of a class to load by propagating the request to the
	 * contained strategies. It iterates through all strategies until a strategy
	 * is able to find the bytes. If none is able to return the bytes null will
	 * be returned.
	 * </p>
	 * 
	 * @param className
	 *            The fully qualified name of the class whose bytes should be
	 *            returned
	 * @return The bytes found for the class or null if no strategy was able to
	 *         deliver the bytes
	 * @throws IOException
	 *             If during the loading of the class files a strategy noticed
	 *             an error
	 */
	private byte[] findClassBytes(String className) throws IOException
	{		
		// propagate the task to the strategies
		byte[] classBytes = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			LOGGER.log(Level.FINE, "find bytes for class {0} in {1} with strategy {2}",
					new Object[] { className, this, strategy });
			classBytes = strategy.findClassBytes(className);
			if (classBytes != null)
				return classBytes;
		}
		return classBytes;
	}

	@Override
	public URL findResource(String resourceName)
	{
		try
		{
			// propagate the task to the strategies
			URL resource = null;
			for (IClassLoaderStrategy strategy : this.strategies)
			{
				resource = strategy.findResource(resourceName);
				if (resource != null)
				{
					return resource;
				}
			}
			return resource;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING,
					"Could not find resource with name {0} in classpath",
					new Object[] { resourceName });
			return null;
		}
	}

	@Override
	public Enumeration<URL> findResources(String resourceName)
			throws IOException
	{
		// propagate the task to the strategies
		Set<URL> foundItems = new HashSet<>();
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			try
			{
				Enumeration<URL> enumerationEnum = strategy
						.findResources(resourceName);
				if (enumerationEnum != null)
				{
					while (enumerationEnum.hasMoreElements())
					{
						URL url = enumerationEnum.nextElement();
						foundItems.add(url);
					}
				}
			}
			catch (IOException e)
			{
				LOGGER.log(
						Level.WARNING,
						"Could not find resources with name {0} in classpath due to {1}",
						new Object[] { resourceName, e.getLocalizedMessage() });
			}
		}
		if (foundItems.isEmpty())
			return null;

		return new IteratorEnumeration<URL>(foundItems.iterator());
	}

	@Override
	protected String findLibrary(String libname)
	{
		// propagate the task to the strategies
		String absolutPath = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			String path = strategy.findLibraryPath(libname);
			if (path != null)
				return path;
		}
		return absolutPath;
	}

	/**
	 * <p>
	 * Returns an {@link InputStream} to the resource rather than returning the
	 * whole object.
	 * </p>
	 * <p>
	 * This method is preferably if the resource to load is larger as the memory
	 * consumption will be drastically lower than on loading the whole file at
	 * once. However the file is locked as long as the InputStream is not
	 * closed.
	 * </p>
	 * 
	 * @param resourceName
	 *            The resource to create an InputStream for
	 * @return The InputStream for the resource
	 * 
	 * @throws IOException
	 *             If during loading a resource an exception occurred
	 */
	protected InputStream findResourceAsStream(String resourceName)
			throws IOException
	{
		// propagate the task to the strategies
		InputStream stream = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			stream = strategy.findResourceAsStream(resourceName);
			if (stream != null)
				return stream;
		}
		return stream;
	}
	
	/**
	 * <p>
	 * Returns the class with the provided name if this instance has loaded the
	 * requested class. If this classloader hasn't loaded that class it will
	 * return <em>null</em>.
	 * </p>
	 * 
	 * @param className
	 *            The name of the class to check if it was loaded
	 * @return The class object of the class which corresponds the given name or
	 *         null if no class with the given name was loaded by this
	 *         classloader instance
	 */
	public Class<?> hasLoadedClass(String className)
	{
		Class<?> clazz = this.findLoadedClass(className);
		if (clazz != null)
			LOGGER.log(Level.INFO, "{0} loaded class {1}/{2}", 
					new Object[] { this.name, className, clazz });
		else
			LOGGER.log(Level.INFO, "{0} did not load class {1}", 
					new Object[] { this.name, className });
		
		return clazz;
	}
}
