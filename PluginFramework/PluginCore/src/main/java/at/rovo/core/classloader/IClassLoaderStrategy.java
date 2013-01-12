package at.rovo.core.classloader;

import java.net.URL;
import java.util.Enumeration;

/**
 * <p><code>IClassLoaderStrategy</code> is a strategy-pattern for the 
 * {@link StrategyClassLoader}.</p>
 * <p>It defines methods for how classes or resources should be read.</p>
 * 
 * @author Roman Vottner
 * @version 0.1
 */
public interface IClassLoaderStrategy
{
	/**
     * <p>Return byte array (which will be turned into a Class instance
     * via {@link ClassLoader#defineClass()}) for class.</p>
     * <p>This method is called by the {@link StrategyClassLoader#findClass(String)}-method, 
     * which needs a certain .class or .jar-file as byte-array.</p>
     * @param name Represents the full name of a .class or a .jar-File  
     * @return Returns a byte-array representing the .class or .jar-File, which 
     *         should be loaded by {@link StrategyClassLoader#findClass(String)}
     */
	public byte[] findClassBytes(String name);
	
	/**
	 * <p>Return URL for resource given by resourceName.</p>
	 * @param resourceName Name of the resource whose URL should be returned.
	 * @return URL of the resource.
	 */
	public URL findResourceURL(String resourceName);
	
	/**
	 * <p>Return Enumeration of resources corresponding to resourceName</p>
	 * @param resourceName Patternname of resources whose URLs should be returned
	 * @return Returns an Enumeration of URLs defining resources that match a certain name
	 */
	public Enumeration<URL> findResourcesEnum(String resourceName);
	
	/**
	 * <p>Return full path to native library given by the name libraryName.</p>
	 * @param libraryName Name of the library whose full path should be returned.
	 * @return Returns the full path of a library
	 */
	public String findLibraryPath(String libraryName);	
}
