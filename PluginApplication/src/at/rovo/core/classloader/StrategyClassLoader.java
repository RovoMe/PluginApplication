package at.rovo.core.classloader;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class StrategyClassLoader<T> extends ClassLoader implements IClassLoaderStrategy
{
	private Set<IClassLoaderStrategy> loaders = null;
	
	public StrategyClassLoader()
	{
		this(StrategyClassLoader.class.getClassLoader(), new HashSet<IClassLoaderStrategy>());
	}
	
	public StrategyClassLoader(Set<IClassLoaderStrategy> loaders)
	{
		this(StrategyClassLoader.class.getClassLoader(), loaders);
	}
	
	public StrategyClassLoader(ClassLoader loader)
	{
		this(loader, new HashSet<IClassLoaderStrategy>());
	}
	
	public StrategyClassLoader(ClassLoader parent, Set<IClassLoaderStrategy> loaders )
	{
		super(parent);
//		for (IClassLoaderStrategy loader : loaders)
//		{
//			this.loaders.add(loader);
//		}
		this.loaders = loaders;
	}
	
	public void addLoader(IClassLoaderStrategy strategy)
	{
		this.loaders.add(strategy);
	}
	
	public Set<IClassLoaderStrategy> getLoaders()
	{
		return loaders;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<T> loadClass(String name)
	{
		try 
		{
			Class<T> clazz = null;
			clazz = (Class<T>) super.loadClass(name);
			if (clazz == null)
			{
				clazz = (Class<T>)this.findClass(name);
			}
			return clazz;
		} 
		catch (ClassNotFoundException e) 
		{
			System.err.println("StrategyClassLoader.loadClass("+name+"): "+e.getLocalizedMessage());
			return null;
		}
	}
	
	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException
	{
		byte[] classBytes = this.findClassBytes(className);
		if (classBytes != null)
//			throw new ClassNotFoundException();
			return defineClass(className, classBytes, 0, classBytes.length);
		return null;
	}

	@Override
	public byte[] findClassBytes(String className)
	{
		byte[] classBytes = null;
		for (IClassLoaderStrategy strategy : this.loaders)
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
		URL resource = null;
		for (IClassLoaderStrategy strategy : loaders)
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
		Enumeration<URL> enumerationEnum = null;
		for (IClassLoaderStrategy strategy : loaders)
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
		String libPath = null;
		for (IClassLoaderStrategy strategy : loaders)
		{
			libPath = strategy.findLibraryPath(libraryName);
			if (libPath != null)
				return libPath;
		}
		return libPath;
	}
}
