package at.rovo.core.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * <p>
 * <code>IClassLoaderStrategy</code> is a strategy-pattern for the
 * {@link StrategyClassLoader}.
 * </p>
 * <p>
 * It defines methods for how classes or resources should be read.
 * </p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IClassLoaderStrategy
{
	/**
	 * <p>
	 * Return byte array (which will be turned into a Class instance via
	 * {@link ClassLoader#defineClass()}) for class.
	 * </p>
	 * <p>
	 * This method is called by the
	 * {@link StrategyClassLoader#findClass(String)}-method, which needs a
	 * certain .class or .jar-file as byte-array.
	 * </p>
	 * 
	 * @param name
	 *            Represents the full name of a .class or a .jar-File
	 * @return Returns a byte-array representing the .class or .jar-File, which
	 *         should be loaded by {@link StrategyClassLoader#findClass(String)}
	 * 
	 * @throws IOException
	 *             If during loading a resource an exception occurred
	 */
	public byte[] findClassBytes(String name) throws IOException;

	/**
	 * <p>
	 * Return URL for resource given by resourceName.
	 * </p>
	 * 
	 * @param resourceName
	 *            Name of the resource whose URL should be returned.
	 * @return URL of the resource.
	 * 
	 * @throws IOException 
	 *             If during loading a resource an exception occurred
	 */
	public URL findResource(String resourceName) throws IOException;

	/**
	 * <p>
	 * Return {@link Enumeration} of resources corresponding to resourceName
	 * </p>
	 * 
	 * @param resourceName
	 *            Patternname of resources whose URLs should be returned
	 * @return Returns an Enumeration of URLs defining resources that match a
	 *         certain name
	 *         
	 * @throws IOException
	 *             If during loading a resource an exception occurred
	 */
	public Enumeration<URL> findResources(String resourceName) 
			throws IOException;

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
	public InputStream findResourceAsStream(String resourceName)
			throws IOException;
	
	/**
	 * <p>
	 * Return full path to native library given by the name libraryName.
	 * </p>
	 *
	 * @param libraryName
	 * Name of the library whose full path should be returned.
	 * @return Returns the full path of a library
	 */
	public String findLibraryPath(String libraryName);
}
