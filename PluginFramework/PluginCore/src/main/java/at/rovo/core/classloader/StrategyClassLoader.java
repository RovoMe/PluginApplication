package at.rovo.core.classloader;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import at.rovo.plugin.PluginException;

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
 * @param <T>
 * @author Roman Vottner
 * @version 0.1
 */
public class StrategyClassLoader<T> extends ClassLoader implements IClassLoaderStrategy
{
	/** The logger of this class **/
	private static Logger logger = Logger.getLogger(StrategyClassLoader.class.getName());
	/** The registered strategies for this class loader **/
	private Set<IClassLoaderStrategy> strategies = null;
	
	/**
	 * <p>
	 * Creates a new instance of this class loader which is a child of the class
	 * loader that loaded this class loder's class file. This constructor will
	 * add an empty {@link Set} for storing strategies to use for class loading.
	 * </p>
	 */
	public StrategyClassLoader()
	{
		this(StrategyClassLoader.class.getClassLoader(), new HashSet<IClassLoaderStrategy>());
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
	public StrategyClassLoader(ClassLoader parent, Set<IClassLoaderStrategy> strategies )
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
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<T> loadClass(String name)
	{
		try 
		{
			return (Class<T>) super.loadClass(name);
		} 
		catch (ClassNotFoundException e) 
		{
			logger.log(Level.SEVERE, "Could not find class: {0}", new Object[] {name});
			throw new PluginException("StrategyClassLoader.loadClass("+name+"): "+e.getLocalizedMessage());
		}
	}
	
	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException
	{
		// propagate the call to the registered strategies
		byte[] classBytes = this.findClassBytes(className);
		if (classBytes != null)
		{
			logger.log(Level.FINE, "found bytes for class {0} - defining class", new Object[] {className});
			// at least one strategy was able to find bytes for this class
			// so create the class based on the found bytes
			return defineClass(className, classBytes, 0, classBytes.length);
		}
		return null;
	}

	@Override
	public byte[] findClassBytes(String className)
	{
		// propagate the task to the strategies
		byte[] classBytes = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			classBytes = strategy.findClassBytes(className);
			if (classBytes != null)
				return classBytes;
		}
		return classBytes;
	}

	@Override
	public URL findResourceURL(String resourceName)
	{
		// propagate the task to the strategies
		URL resource = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			resource = strategy.findResourceURL(resourceName);
			if (resource != null)
				return resource;
		}
		return resource;
	}

	@Override
	public Enumeration<URL> findResourcesEnum(String resourceName)
	{
		// propagate the task to the strategies
		Enumeration<URL> enumerationEnum = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			enumerationEnum = strategy.findResourcesEnum(resourceName);
			if (enumerationEnum != null)
				return enumerationEnum;
		}
		return enumerationEnum;
	}

	@Override
	public String findLibraryPath(String libraryName)
	{
		// propagate the task to the strategies
		String libPath = null;
		for (IClassLoaderStrategy strategy : this.strategies)
		{
			libPath = strategy.findLibraryPath(libraryName);
			if (libPath != null)
				return libPath;
		}
		return libPath;
	}
}
